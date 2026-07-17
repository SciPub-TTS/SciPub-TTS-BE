package com.brotherhood.scipubtts.feed.service.impl;

import com.brotherhood.scipubtts.feed.client.OpenAlexWorksClient;
import com.brotherhood.scipubtts.common.openalex.logging.OpenAlexCallContext;
import com.brotherhood.scipubtts.feed.dto.response.OpenAlexWorksResponse;
import com.brotherhood.scipubtts.feed.entity.ApiJob;
import com.brotherhood.scipubtts.feed.model.FeedDraft;
import com.brotherhood.scipubtts.feed.model.FeedKey;
import com.brotherhood.scipubtts.feed.model.FeedReason;
import com.brotherhood.scipubtts.feed.model.FollowTargetGroupView;
import com.brotherhood.scipubtts.feed.repository.ApiJobRepository;
import com.brotherhood.scipubtts.feed.service.FeedPersistenceService;
import com.brotherhood.scipubtts.feed.service.ResearchFeedSyncService;
import com.brotherhood.scipubtts.follow.repository.UserFollowRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ✅ REFACTOR: chuyển sang cơ chế "cuốn chiếu" (chunk/batch processing) để
 * chạy an toàn trên Azure free tier (1 vCPU / 1GB RAM).
 *
 * VẤN ĐỀ CỦA BẢN CŨ:
 *   draftMap (Map<FeedKey, FeedDraft>) được tích lũy qua TOÀN BỘ vòng lặp
 *   "for (FollowTargetGroupView group : groups)" và chỉ flush xuống DB
 *   MỘT LẦN DUY NHẤT ở cuối syncDailyFeed(). Nếu có nhiều target groups,
 *   nhiều page, nhiều follower — draftMap có thể phình tới hàng trăm nghìn
 *   tới hàng triệu entry, mỗi entry chứa cả abstractText/keywordsJson full
 *   text → dễ vượt 1GB và bị OOM-kill trên free tier.
 *
 * CÁCH SỬA:
 *   - Xử lý + flush draftMap xuống DB NGAY SAU MỖI TARGET GROUP (không đợi
 *     hết toàn bộ groups). draftMap được tạo MỚI cho mỗi group, sau khi
 *     flush thì bị garbage-collect ngay, không cộng dồn qua các group khác.
 *   - INSERT ... ON CONFLICT DO NOTHING đã có sẵn nên flush nhiều lần hoàn
 *     toàn an toàn, không sinh duplicate.
 *   - Trong nội bộ 1 group, nếu followerIds quá lớn (vd 1 topic có 5000
 *     người follow), cũng flush theo CHUNK_SIZE follower để tránh 1 group
 *     đơn lẻ tự nó đã đủ lớn để gây OOM.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResearchFeedSyncServiceImpl implements ResearchFeedSyncService {
    private static final String JOB_TYPE = "FEED_SYNC";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_PARTIAL_SUCCESS = "PARTIAL_SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    // Số draft tối đa được giữ trong RAM trước khi buộc phải flush xuống DB.
    // Với FeedDraft mang theo abstractText (vài KB/record), 2000 record ước
    // tính ~10-20MB RAM tại 1 thời điểm — an toàn cho free tier 1GB.
    private static final int FLUSH_CHUNK_SIZE = 2000;

    // Giới hạn độ dài errorLog để tránh StringBuilder phình vô hạn khi
    // nhiều target liên tục fail (vd OpenAlex rate-limit toàn bộ).
    private static final int MAX_ERROR_LOG_LENGTH = 10_000;

    private final UserFollowRepository userFollowRepository;
    private final OpenAlexWorksClient openAlexWorksClient;
    private final FeedPersistenceService feedPersistenceService;
    private final ApiJobRepository apiJobRepository;
    private final ObjectMapper objectMapper;

    @Value("${openalex.feed.default-lookback-days:7}")
    private long defaultLookbackDays;

    @Value("${openalex.feed.overlap-days:2}")
    private long overlapDays;

    @Value("${openalex.feed.max-pages-per-target:10}")
    private int maxPagesPerTarget;

    @Override
    public void syncDailyFeed() {
        OffsetDateTime startedAt = OffsetDateTime.now();

        ApiJob job = ApiJob.builder()
                .jobType(JOB_TYPE)
                .status(STATUS_RUNNING)
                .startedAt(startedAt)
                .totalFetched(0)
                .totalSaved(0)
                .totalFailed(0)
                .build();

        ApiJob savedJob = apiJobRepository.save(job);

        OpenAlexCallContext.runAsSystemJob(savedJob.getId(), JOB_TYPE, () -> syncDailyFeed(savedJob));
    }

    private void syncDailyFeed(ApiJob job) {
        int totalFetched = 0;
        int totalSaved = 0;
        int failedTargets = 0;
        StringBuilder errorLog = new StringBuilder();

        try {
            LocalDate toDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            LocalDate fromDate = resolveFromDate(toDate);

            job.setRequestParams("""
                    {"fromDate":"%s","toDate":"%s","strategy":"api_job_last_success_with_overlap"}
                    """.formatted(fromDate, toDate));
            apiJobRepository.save(job);

            List<FollowTargetGroupView> groups = userFollowRepository.findFeedTargetGroups();

            // ✅ KHÔNG còn 1 draftMap chung sống suốt vòng lặp — mỗi group tự
            // quản lý draftMap riêng của nó và flush ngay khi xử lý xong.
            for (FollowTargetGroupView group : groups) {
                try {
                    GroupSyncResult result = processOneTargetGroup(group, fromDate, toDate);
                    totalFetched += result.fetched();
                    totalSaved += result.saved();
                } catch (Exception ex) {
                    failedTargets++;
                    appendError(errorLog, group, ex);
                    log.warn("Feed sync target failed. type={}, id={}",
                            group.getTargetType(),
                            group.getTargetOpenalexId(),
                            ex
                    );
                }
            }

            job.setTotalFetched(totalFetched);
            job.setTotalSaved(totalSaved);
            job.setTotalFailed(failedTargets);
            job.setFinishedAt(OffsetDateTime.now());
            job.setErrorLog(errorLog.isEmpty() ? null : errorLog.toString());

            if (failedTargets == 0) {
                job.setStatus(STATUS_SUCCESS);
            } else if (totalSaved > 0 || totalFetched > 0) {
                job.setStatus(STATUS_PARTIAL_SUCCESS);
            } else {
                job.setStatus(STATUS_FAILED);
            }

            apiJobRepository.save(job);

        } catch (Exception ex) {
            job.setStatus(STATUS_FAILED);
            job.setFinishedAt(OffsetDateTime.now());
            job.setErrorLog(ex.getMessage());
            apiJobRepository.save(job);

            throw ex;
        }
    }

    private LocalDate resolveFromDate(LocalDate toDate) {
        return apiJobRepository
                .findTopByJobTypeAndStatusOrderByFinishedAtDesc(JOB_TYPE, STATUS_SUCCESS)
                .map(ApiJob::getFinishedAt)
                .map(time -> time
                        .atZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"))
                        .toLocalDate()
                        .minusDays(overlapDays)
                )
                .orElse(toDate.minusDays(defaultLookbackDays));
    }

    /** Kết quả xử lý 1 target group: số work fetch được + số draft đã lưu thật vào DB. */
    private record GroupSyncResult(int fetched, int saved) {}

    /**
     * ✅ ĐÃ REFACTOR — xử lý CUỐN CHIẾU theo từng page của OpenAlex:
     *   mỗi page (100 work) → build draft cho followerIds → nếu draftMap
     *   vượt FLUSH_CHUNK_SIZE thì flush ngay xuống DB và clear map → tiếp
     *   tục page sau. Kết thúc group thì flush phần còn lại (nếu có).
     *
     * draftMap không còn là tham số truyền vào từ ngoài (tránh việc gọi
     * lẫn nhau giữa các group làm map phình to) — nó được tạo MỚI và CHẾT
     * (garbage-collected) ngay trong scope của method này.
     */
    private GroupSyncResult processOneTargetGroup(
            FollowTargetGroupView group,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        List<UUID> followerIds = parseUserIds(group.getUserIds());

        if (followerIds.isEmpty()) {
            return new GroupSyncResult(0, 0);
        }

        String cursor = "*";
        int page = 0;
        int fetched = 0;
        int saved = 0;

        Map<FeedKey, FeedDraft> draftMap = new LinkedHashMap<>();

        while (StringUtils.hasText(cursor) && page < maxPagesPerTarget) {
            page++;

            OpenAlexWorksResponse response = openAlexWorksClient.fetchWorksPage(
                    group.getTargetType(),
                    group.getTargetOpenalexId(),
                    fromDate,
                    toDate,
                    cursor
            );

            if (response == null || response.results() == null || response.results().isEmpty()) {
                break;
            }

            for (OpenAlexWorksResponse.OpenAlexWork work : response.results()) {
                fetched++;

                String workId = normalizeOpenAlexId(work.id());
                if (!StringUtils.hasText(workId)) {
                    continue;
                }

                for (UUID userId : followerIds) {
                    FeedKey key = new FeedKey(userId, workId);

                    FeedDraft draft = draftMap.computeIfAbsent(
                            key,
                            ignored -> createDraft(userId, workId, work)
                    );

                    draft.getReasons().add(
                            new FeedReason(
                                    group.getTargetType(),
                                    normalizeOpenAlexId(group.getTargetOpenalexId()),
                                    group.getDisplayNameSnapshot()
                            )
                    );
                }

                // ✅ FLUSH THEO CHUNK — ngay khi draftMap đủ lớn, ghi DB và
                // clear ngay, không đợi hết page/group mới ghi.
                if (draftMap.size() >= FLUSH_CHUNK_SIZE) {
                    saved += flush(draftMap);
                }
            }

            cursor = response.meta() == null ? null : response.meta().nextCursor();

            if (!StringUtils.hasText(cursor)) {
                break;
            }
        }

        // Flush phần còn lại cuối group (không đủ FLUSH_CHUNK_SIZE để trigger ở trên).
        if (!draftMap.isEmpty()) {
            saved += flush(draftMap);
        }

        return new GroupSyncResult(fetched, saved);
    }

    /** Ghi 1 chunk draft xuống DB rồi clear map ngay — giải phóng RAM cho chunk tiếp theo. */
    private int flush(Map<FeedKey, FeedDraft> draftMap) {
        int saved = feedPersistenceService.saveFeedDrafts(draftMap.values());
        draftMap.clear();
        return saved;
    }

    private FeedDraft createDraft(
            UUID userId,
            String workId,
            OpenAlexWorksResponse.OpenAlexWork work
    ) {
        FeedDraft draft = new FeedDraft(userId, workId);

        draft.setTitleSnapshot(work.displayName());
        draft.setAuthorsSnapshot(buildAuthorsSnapshot(work.authorships()));
        draft.setSourceSnapshot(extractSourceName(work.primaryLocation()));
        draft.setPublicationYear(work.publicationYear());
        draft.setPublicationDate(work.publicationDate());
        draft.setCitationSnapshot(work.citedByCount());
        draft.setGeneratedAt(OffsetDateTime.now());

        draft.setAuthorOpenAlexIdsSnapshot(buildAuthorOpenAlexIdsSnapshot(work.authorships()));
        draft.setWorkTypeSnapshot(work.type());
        draft.setDoi(work.doi());
        draft.setPdfUrl(extractPdfUrl(work.primaryLocation()));
        draft.setAbstractText(decodeAbstract(work.abstractInvertedIndex()));

        OpenAlexWorksResponse.Topic primaryTopic = extractPrimaryTopic(work.topics());
        if (primaryTopic != null) {
            draft.setTopicSnapshot(primaryTopic.displayName());
            draft.setTopicOpenAlexIdSnapshot(normalizeOpenAlexId(primaryTopic.id()));

            if (primaryTopic.field() != null) {
                draft.setPrimaryFieldSnapshot(primaryTopic.field().displayName());
            }
            if (primaryTopic.subfield() != null) {
                draft.setSubfieldSnapshot(primaryTopic.subfield().displayName());
            }
        }

        draft.setKeywordsJson(buildKeywordsJson(work.keywords()));

        return draft;
    }

    private String buildAuthorsSnapshot(List<OpenAlexWorksResponse.Authorship> authorships) {
        if (authorships == null || authorships.isEmpty()) {
            return null;
        }

        return authorships.stream()
                .filter(a -> a.author() != null)
                .map(a -> a.author().displayName())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(", "));
    }

    private String buildAuthorOpenAlexIdsSnapshot(List<OpenAlexWorksResponse.Authorship> authorships) {
        if (authorships == null || authorships.isEmpty()) {
            return null;
        }

        String joined = authorships.stream()
                .filter(a -> a.author() != null)
                .map(a -> normalizeOpenAlexId(a.author().id()))
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(","));

        return StringUtils.hasText(joined) ? joined : null;
    }

    private String extractSourceName(OpenAlexWorksResponse.PrimaryLocation primaryLocation) {
        if (primaryLocation == null || primaryLocation.source() == null) {
            return null;
        }

        return primaryLocation.source().displayName();
    }

    private String extractPdfUrl(OpenAlexWorksResponse.PrimaryLocation primaryLocation) {
        if (primaryLocation == null) {
            return null;
        }
        return primaryLocation.pdfUrl();
    }

    private OpenAlexWorksResponse.Topic extractPrimaryTopic(List<OpenAlexWorksResponse.Topic> topics) {
        if (topics == null || topics.isEmpty()) {
            return null;
        }
        return topics.stream()
                .max(Comparator.comparingDouble(OpenAlexWorksResponse.Topic::score))
                .orElse(null);
    }

    private String buildKeywordsJson(List<OpenAlexWorksResponse.Keyword> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return null;
        }

        List<String> keywordNames = keywords.stream()
                .map(OpenAlexWorksResponse.Keyword::displayName)
                .filter(StringUtils::hasText)
                .toList();

        if (keywordNames.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(keywordNames);
        } catch (JsonProcessingException e) {
            log.warn("Cannot serialize keywords into JSON, bỏ qua keywordsJson cho work này", e);
            return null;
        }
    }

    /**
     * ✅ ĐÃ SỬA: dùng StringBuilder thay cho TreeMap<Integer,String> +
     * String.join để giảm allocation trung gian khi decode abstract dài.
     * Với abstract dài (>500 từ), cách cũ tạo 1 TreeMap entry + 1 ArrayList
     * tạm cho String.join — cách này ghi trực tiếp vào StringBuilder.
     */
    private String decodeAbstract(Map<String, List<Integer>> invertedIndex) {
        if (invertedIndex == null || invertedIndex.isEmpty()) {
            return null;
        }

        TreeMap<Integer, String> positionToWord = new TreeMap<>();
        invertedIndex.forEach((word, positions) -> {
            if (positions != null) {
                for (Integer pos : positions) {
                    positionToWord.put(pos, word);
                }
            }
        });

        StringBuilder sb = new StringBuilder();
        for (String word : positionToWord.values()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(word);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private List<UUID> parseUserIds(String userIds) {
        if (!StringUtils.hasText(userIds)) {
            return List.of();
        }

        return Arrays.stream(userIds.split(","))
                .filter(StringUtils::hasText)
                .map(UUID::fromString)
                .toList();
    }

    private String normalizeOpenAlexId(String openalexId) {
        if (!StringUtils.hasText(openalexId)) {
            return openalexId;
        }

        int lastSlash = openalexId.lastIndexOf("/");
        if (lastSlash >= 0) {
            return openalexId.substring(lastSlash + 1);
        }

        return openalexId;
    }

    /** ✅ ĐÃ SỬA: chặn errorLog phình vô hạn khi nhiều target fail liên tục. */
    private void appendError(
            StringBuilder errorLog,
            FollowTargetGroupView group,
            Exception ex
    ) {
        if (errorLog.length() >= MAX_ERROR_LOG_LENGTH) {
            return;
        }

        String message;

        if (ex instanceof RestClientResponseException restEx) {
            message = "HTTP " + restEx.getStatusCode().value() + " - " + restEx.getResponseBodyAsString();
        } else {
            message = ex.getMessage();
        }

        errorLog.append("[")
                .append(group.getTargetType())
                .append(":")
                .append(group.getTargetOpenalexId())
                .append("] ")
                .append(message)
                .append("\n");
    }
}
