package com.brotherhood.scipubtts.socialhub.service.impl;

import com.brotherhood.scipubtts.bookmark.entity.UserBookmark;
import com.brotherhood.scipubtts.bookmark.repository.UserBookmarkRepository;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.socialhub.dto.request.CreateSocialPostRequest;
import com.brotherhood.scipubtts.socialhub.dto.request.PostReferenceRequest;
import com.brotherhood.scipubtts.socialhub.dto.request.UpdateSocialPostRequest;
import com.brotherhood.scipubtts.socialhub.dto.response.LikeToggleResponse;
import com.brotherhood.scipubtts.socialhub.dto.response.SocialPostDetailResponse;
import com.brotherhood.scipubtts.socialhub.dto.response.SocialPostSummaryResponse;
import com.brotherhood.scipubtts.socialhub.entity.SocialPost;
import com.brotherhood.scipubtts.socialhub.entity.SocialPostLike;
import com.brotherhood.scipubtts.socialhub.entity.SocialPostReference;
import com.brotherhood.scipubtts.socialhub.repository.SocialPostLikeRepository;
import com.brotherhood.scipubtts.socialhub.repository.SocialPostReferenceRepository;
import com.brotherhood.scipubtts.socialhub.repository.SocialPostRepository;
import com.brotherhood.scipubtts.socialhub.service.SocialService;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class SocialServiceImpl implements SocialService {

    private static final int MAX_REFERENCES = 3;
    private static final int BODY_PREVIEW_LENGTH = 200;

    private final SocialPostRepository postRepository;
    private final SocialPostLikeRepository likeRepository;
    private final SocialPostReferenceRepository referenceRepository;
    private final UserRepository userRepository;
    private final UserBookmarkRepository bookmarkRepository;

    // ─────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SocialPostDetailResponse createPost(UUID authorId, CreateSocialPostRequest request) {

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // FLOW 1: validate + lấy Bookmark hợp lệ
        List<UserBookmark> validBookmarks = validateAndExtractBookmarks(authorId, request.references());

        SocialPost post = SocialPost.builder()
                .author(author)
                .title(request.title().trim())
                .body(request.body().trim())
                .build();

        List<SocialPostReference> refs = validBookmarks.stream()
                .map(bm -> buildReferenceFromBookmark(post, bm))
                .collect(Collectors.toList());
        post.setReferences(refs);

        SocialPost saved = postRepository.save(post);
        return toDetailResponse(saved, false, false);
    }

    // ─────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<SocialPostSummaryResponse> getNewest(Pageable pageable, UUID viewerId) {
        Page<SocialPost> page = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        return toSummaryPage(page, viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SocialPostSummaryResponse> getTop(Pageable pageable, UUID viewerId) {
        Page<SocialPost> page = postRepository.findAllByOrderByLikeCountDescCreatedAtDesc(pageable);
        return toSummaryPage(page, viewerId);
    }

    // ─────────────────────────────────────────────────────────
    // UPDATE  — FLOW 3
    // ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SocialPostDetailResponse updatePost(UUID postId, UUID editorId, UpdateSocialPostRequest request) {
        SocialPost post = findActivePost(postId);
        assertIsAuthor(post, editorId);

        if (StringUtils.hasText(request.title())) post.setTitle(request.title().trim());
        if (StringUtils.hasText(request.body()))  post.setBody(request.body().trim());

        boolean likesReset = false;

        // Chỉ xử lý reference nếu FE có gửi field này lên (null = giữ nguyên)
        if (request.references() != null) {

            // Bước 4.1: so sánh openalexId cũ vs mới
            Set<String> oldOpenalexIds = new HashSet<>(referenceRepository.findOpenalexIdByPostId(postId));
            Set<String> newOpenalexIds = request.references().stream()
                    .map(r -> normalizeOpenAlexId(r.openalexId()))
                    .collect(Collectors.toSet());

            boolean referencesChanged = !oldOpenalexIds.equals(newOpenalexIds);

            if (referencesChanged) {
                // Bước 4.2: reset toàn bộ like
                likeRepository.deleteAllByPostId(postId);
                post.resetLikeCount();
                likesReset = true;

                // Validate + lấy bookmark hợp lệ cho danh sách mới
                List<UserBookmark> validBookmarks = validateAndExtractBookmarks(editorId, request.references());

                // Xóa reference cũ, thêm reference mới
                post.getReferences().clear();
                List<SocialPostReference> newRefs = validBookmarks.stream()
                        .map(bm -> buildReferenceFromBookmark(post, bm))
                        .toList();
                post.getReferences().addAll(newRefs);
            }
            // Bước 4.3: nếu giống nhau → không đổi gì về reference, likesReset = false
        }

        SocialPost updated = postRepository.save(post);

        // Nếu vừa reset like thì chắc chắn editor (chính là author) chưa like bài của mình theo trạng thái mới
        boolean liked = !likesReset && likeRepository.existsByPostIdAndUserId(postId, editorId);

        return toDetailResponse(updated, liked, likesReset);
    }

    // ─────────────────────────────────────────────────────────
    // DELETE (soft)
    // ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deletePost(UUID postId, UUID requesterId) {
        SocialPost post = findActivePost(postId);

        if (!post.getAuthor().getId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        post.softDelete();
        postRepository.save(post);
    }

    // ─────────────────────────────────────────────────────────
    // LIKE TOGGLE — INSERT/DELETE like + UPDATE like_count cộng dồn
    // tại DB, cùng nằm trong 1 @Transactional để tránh race condition
    // ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LikeToggleResponse toggleLike(UUID postId, UUID userId) {
        SocialPost post = findActivePost(postId);

        Optional<SocialPostLike> existing = likeRepository.findByPostIdAndUserId(postId, userId);

        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            postRepository.decrementLikeCount(postId);

            int newCount = findActivePost(postId).getLikeCount();
            return new LikeToggleResponse(false, newCount);

        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            SocialPostLike like = SocialPostLike.builder()
                    .post(post)
                    .user(user)
                    .build();

            likeRepository.save(like);
            postRepository.incrementLikeCount(postId);

            int newCount = findActivePost(postId).getLikeCount();
            return new LikeToggleResponse(true, newCount);
        }
    }

    // ─────────────────────────────────────────────────────────
    // FLOW 1 — Helper validate References (dùng chung Create & Update)
    // ─────────────────────────────────────────────────────────

    /**
     * Validate danh sách reference gửi lên và trả về Bookmark tương ứng
     * (đã xác nhận thuộc sở hữu của userId) để map sang SocialPostReference.
     * Quy tắc:
     * 1. null/empty  → trả về list rỗng.
     * 2. size > 3    → throw EXCEEDS_LIMIT.
     * 3. Lọc duy nhất theo openalexId (chuẩn hóa) — không cho trùng trong cùng 1 bài.
     * 4. openalexId không bắt đầu bằng "W" → throw INVALID_FORMAT.
     * 5. openalexId phải nằm trong Bookmark của chính userId này
     *    → nếu thiếu bất kỳ ID nào, throw REFERENCE_NOT_IN_BOOKMARK.
     */
    private List<UserBookmark> validateAndExtractBookmarks(UUID userId, List<PostReferenceRequest> refs) {

        if (refs == null || refs.isEmpty()) {
            return List.of();
        }

        if (refs.size() > MAX_REFERENCES) {
            throw new BusinessException(ErrorCode.SOCIAL_POST_EXCEEDS_REFERENCE_LIMIT);
        }

        // Chuẩn hóa + loại trùng
        List<String> normalizedIds = refs.stream()
                .map(r -> normalizeOpenAlexId(r.openalexId()))
                .distinct()
                .toList();

        // Check định dạng "W..."
        boolean hasInvalidFormat = normalizedIds.stream()
                .anyMatch(id -> id == null || !id.startsWith("W"));
        if (hasInvalidFormat) {
            throw new BusinessException(ErrorCode.SOCIAL_POST_REFERENCE_INVALID_FORMAT);
        }

        // Check nguồn gốc Bookmark
        List<UserBookmark> validBookmarks = bookmarkRepository.findByUserIdAndOpenAlexIdIn(userId, normalizedIds);

        if (validBookmarks.size() < normalizedIds.size()) {
            throw new BusinessException(ErrorCode.SOCIAL_POST_REFERENCE_NOT_IN_BOOKMARK);
        }

        return validBookmarks;
    }

    // ─────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────

    private SocialPost findActivePost(UUID postId) {
        // @SQLRestriction tự filter deleted_at IS NULL
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOCIAL_POST_NOT_FOUND));
    }

    private void assertIsAuthor(SocialPost post, UUID userId) {
        if (!post.getAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    /**
     * Chuẩn hóa OpenAlex ID — đồng bộ với module Bookmark.
     * Input:  "https://openalex.org/W123456789" hoặc "W123456789" hoặc "w123456789"
     * Output: "W123456789"
     */
    private String normalizeOpenAlexId(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.contains("/")) {
            trimmed = trimmed.substring(trimmed.lastIndexOf('/') + 1);
        }
        return trimmed.toUpperCase();
    }

    /**
     * Lấy snapshot trực tiếp từ Bookmark đã xác thực — KHÔNG dùng dữ liệu
     * FE gửi lên, KHÔNG gọi lại OpenAlex API.
     */
    private SocialPostReference buildReferenceFromBookmark(SocialPost post, UserBookmark bookmark) {
        return SocialPostReference.builder()
                .post(post)
                .openalexId(bookmark.getOpenAlexId())
                .titleSnapshot(bookmark.getTitleSnapshot())
                .authorsSnapshot(bookmark.getAuthorsSnapshot())
                .sourceSnapshot(bookmark.getSourceSnapshot())
                .yearSnapshot(bookmark.getPublicationYear() != null
                        ? bookmark.getPublicationYear().shortValue() : null)
                .doiSnapshot(null) // user_bookmark hiện không lưu DOI riêng; map nếu sau này có cột
                .build();
    }

    /**
     * Hybrid-view: batch check liked status cho toàn bộ page một lần query
     * thay vì N+1 queries.
     */
    private Page<SocialPostSummaryResponse> toSummaryPage(Page<SocialPost> page, UUID viewerId) {
        List<UUID> postIds = page.stream().map(SocialPost::getId).toList();

        Set<UUID> likedIds = (viewerId != null && !postIds.isEmpty())
                ? likeRepository.findLikedPostIds(viewerId, postIds)
                : Set.of();

        return page.map(post -> toSummaryResponse(post, likedIds.contains(post.getId())));
    }

    private SocialPostSummaryResponse toSummaryResponse(SocialPost post, boolean liked) {
        String preview = post.getBody().length() > BODY_PREVIEW_LENGTH
                ? post.getBody().substring(0, BODY_PREVIEW_LENGTH) + "…"
                : post.getBody();

        User author = post.getAuthor();
        String fullName = (author.getFirstName() + " " + author.getLastName()).trim();

        return new SocialPostSummaryResponse(
                post.getId(),
                post.getTitle(),
                preview,
                post.getLikeCount(),
                liked,
                new SocialPostSummaryResponse.AuthorInfo(author.getId(), fullName),
                post.getCreatedAt()
        );
    }

    private SocialPostDetailResponse toDetailResponse(SocialPost post, boolean liked, boolean likesReset) {
        User author = post.getAuthor();
        String fullName = (author.getFirstName() + " " + author.getLastName()).trim();

        List<SocialPostDetailResponse.ReferenceInfo> refs = post.getReferences().stream()
                .map(r -> new SocialPostDetailResponse.ReferenceInfo(
                        r.getId(),
                        r.getOpenalexId(),
                        r.getTitleSnapshot(),
                        r.getAuthorsSnapshot(),
                        r.getSourceSnapshot(),
                        r.getYearSnapshot() != null ? r.getYearSnapshot().intValue() : null,
                        r.getDoiSnapshot()
                ))
                .toList();

        return new SocialPostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getBody(),
                post.getLikeCount(),
                liked,
                new SocialPostDetailResponse.AuthorInfo(author.getId(), fullName),
                refs,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                likesReset
        );
    }
}
