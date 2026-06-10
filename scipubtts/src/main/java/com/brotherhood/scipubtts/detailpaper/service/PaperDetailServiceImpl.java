package com.brotherhood.scipubtts.detailpaper.service;

import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.search.service.OpenAlexMapReader;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class PaperDetailServiceImpl implements PaperDetailService {

    // This service loads the main paper detail, then enriches it with titles
    // for referenced works and related works so the frontend can render links easily.
    private final OpenAlexClient openAlexClient;
    private final OpenAlexMapReader openAlexMapReader;
    private final PaperDetailOpenAlexQueryFactory paperDetailOpenAlexQueryFactory;

    public PaperDetailServiceImpl(
            OpenAlexClient openAlexClient,
            OpenAlexMapReader openAlexMapReader,
            PaperDetailOpenAlexQueryFactory paperDetailOpenAlexQueryFactory
    ) {
        this.openAlexClient = openAlexClient;
        this.openAlexMapReader = openAlexMapReader;
        this.paperDetailOpenAlexQueryFactory = paperDetailOpenAlexQueryFactory;
    }

    @Override
    public Map<String, Object> getWorkDetail(String workId) {
        Map<String, Object> workDetail = openAlexClient.get(
                paperDetailOpenAlexQueryFactory.buildWorkDetailPath(workId),
                paperDetailOpenAlexQueryFactory.buildWorkDetailQueryParams()
        );

        addReferenceDetails(workDetail, "referenced_works", "referenced_work_details");
        addReferenceDetails(workDetail, "related_works", "related_work_details");

        return workDetail;
    }

    private void addReferenceDetails(
            Map<String, Object> workDetail,
            String sourceKey,
            String targetKey
    ) {
        Object rawWorkIds = workDetail.get(sourceKey);
        List<Map<String, Object>> summaries = buildWorkReferenceSummaries(rawWorkIds);
        workDetail.put(targetKey, summaries);
    }

    private List<Map<String, Object>> buildWorkReferenceSummaries(Object rawWorkIds) {
        // Keep the original order so references appear in a stable order on the frontend.
        List<String> orderedWorkIds = normalizeWorkIds(rawWorkIds);
        if (orderedWorkIds.isEmpty()) {
            return List.of();
        }

        Map<String, String> titlesByWorkId = fetchTitlesByWorkId(orderedWorkIds);
        List<Map<String, Object>> summaries = new ArrayList<>();

        for (String workId : orderedWorkIds) {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("id", workId);
            summary.put("title", titlesByWorkId.getOrDefault(workId, workId));
            summaries.add(summary);
        }

        return summaries;
    }

    private Map<String, String> fetchTitlesByWorkId(List<String> orderedWorkIds) {
        // Remove duplicates before querying OpenAlex to avoid unnecessary requests.
        List<String> uniqueWorkIds = new ArrayList<>(new LinkedHashSet<>(orderedWorkIds));
        Map<String, String> titlesByWorkId = new LinkedHashMap<>();

        // Query in chunks because a work can have many references.
        for (int startIndex = 0; startIndex < uniqueWorkIds.size(); startIndex += 50) {
            int endIndex = Math.min(startIndex + 50, uniqueWorkIds.size());
            List<String> workIdChunk = uniqueWorkIds.subList(startIndex, endIndex);
            Map<String, Object> response = openAlexClient.get(
                    "/works",
                    paperDetailOpenAlexQueryFactory.buildWorkReferenceQueryParams(workIdChunk)
            );

            for (Map<String, Object> result : openAlexMapReader.getMapList(response, "results")) {
                String normalizedWorkId = extractNormalizedWorkId(result);
                if (normalizedWorkId.isBlank()) {
                    continue;
                }

                String title = openAlexMapReader.sanitizeDisplayText(
                        openAlexMapReader.getString(result, "display_name")
                );
                if (title == null || title.isBlank()) {
                    title = openAlexMapReader.sanitizeDisplayText(
                            openAlexMapReader.getString(result, "title")
                    );
                }

                if (title != null && !title.isBlank()) {
                    titlesByWorkId.put(normalizedWorkId, title);
                }
            }
        }

        return titlesByWorkId;
    }

    private List<String> normalizeWorkIds(Object rawWorkIds) {
        // OpenAlex can return ids as full URLs, so we normalize them to the last segment.
        List<String> normalizedWorkIds = new ArrayList<>();

        for (Object rawWorkId : openAlexMapReader.getObjectList(rawWorkIds)) {
            String normalizedWorkId = extractNormalizedWorkId(String.valueOf(rawWorkId));
            if (!normalizedWorkId.isBlank()) {
                normalizedWorkIds.add(normalizedWorkId);
            }
        }

        return normalizedWorkIds;
    }

    private String extractNormalizedWorkId(Map<String, Object> work) {
        return extractNormalizedWorkId(openAlexMapReader.getString(work, "id"));
    }

    private String extractNormalizedWorkId(String rawWorkId) {
        String normalizedWorkId = rawWorkId == null ? "" : rawWorkId.trim();
        int lastSlashIndex = normalizedWorkId.lastIndexOf('/');

        if (lastSlashIndex >= 0) {
            normalizedWorkId = normalizedWorkId.substring(lastSlashIndex + 1);
        }

        return normalizedWorkId.trim();
    }
}
