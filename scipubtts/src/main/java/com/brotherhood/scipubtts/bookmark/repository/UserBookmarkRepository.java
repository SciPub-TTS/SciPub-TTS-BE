package com.brotherhood.scipubtts.bookmark.repository;

import com.brotherhood.scipubtts.bookmark.entity.UserBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserBookmarkRepository extends JpaRepository<UserBookmark, UUID> {

    // ==========================================
    // ENDPOINT 1 & 3: ADD BOOKMARK & CHECK STATUS
    // ==========================================
    Optional<UserBookmark> findByUserIdAndOpenAlexId(UUID userId, String openAlexId);

    boolean existsByUserIdAndOpenAlexId(UUID userId, String openAlexId);
    // ==========================================
    // ENDPOINT 2: GET BOOKMARK LIST (LAZY PAGING + DYNAMIC FILTER)
    // ==========================================
    @Query("""
            SELECT b
            FROM UserBookmark b
            WHERE b.userId = :userId
              AND (:topic IS NULL OR :topic = '' OR b.topicSnapshot = :topic)
              AND (:source IS NULL OR :source = '' OR b.sourceSnapshot = :source)
              AND (:author IS NULL OR :author = '' OR LOWER(b.authorsSnapshot) LIKE LOWER(CONCAT('%', :author, '%')))
              AND (:year IS NULL OR b.publicationYear = :year)
              AND (
                  :keyword IS NULL OR :keyword = ''
                  OR LOWER(b.titleSnapshot) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(b.authorsSnapshot) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(b.sourceSnapshot) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(b.topicSnapshot) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """)
    Page<UserBookmark> searchMyBookmarks(
            @Param("userId") UUID userId,
            @Param("keyword") String keyword,
            @Param("topic") String topic,
            @Param("source") String source,
            @Param("author") String author,
            @Param("year") Integer year,
            Pageable pageable
    );

    // ==========================================
    // ENDPOINT 4: GET STATS
    // ==========================================
    // 4.1. Total papers
    long countByUserId(UUID userId);

    // 4.2. Total unique topics
    @Query("""
        SELECT COUNT(DISTINCT b.topicSnapshot)
        FROM UserBookmark b
        WHERE b.userId = :userId
          AND b.topicSnapshot IS NOT NULL
          AND b.topicSnapshot <> ''
    """)
    long countDistinctTopicsByUserId(@Param("userId") UUID userId);

    // 4.3. Total unique sources
    @Query("""
        SELECT COUNT(DISTINCT b.sourceSnapshot)
        FROM UserBookmark b
        WHERE b.userId = :userId
          AND b.sourceSnapshot IS NOT NULL
          AND b.sourceSnapshot <> ''
    """)
    long countDistinctSourcesByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT COUNT(DISTINCT b.authorsSnapshot)
        FROM UserBookmark b
        WHERE b.userId = :userId
            AND b.authorsSnapshot IS NOT NULL
            AND b.authorsSnapshot <> ''
    """)
    long countDistinctAuthorsByUserId(@Param("userId") UUID userId);


    // ==========================================
    // ENDPOINT 5: GET FILTER OPTIONS
    // ==========================================
    // 5.1. Danh sách Topics (sắp xếp A-Z)
    @Query("""
        SELECT DISTINCT b.topicSnapshot
        FROM UserBookmark b
        WHERE b.userId = :userId
          AND b.topicSnapshot IS NOT NULL
          AND b.topicSnapshot <> ''
        ORDER BY b.topicSnapshot ASC
    """)
    List<String> findDistinctTopicsByUserId(@Param("userId") UUID userId);

    // 5.2. Danh sách Sources (sắp xếp A-Z)
    @Query("""
        SELECT DISTINCT b.sourceSnapshot
        FROM UserBookmark b
        WHERE b.userId = :userId
          AND b.sourceSnapshot IS NOT NULL
          AND b.sourceSnapshot <> ''
        ORDER BY b.sourceSnapshot ASC
    """)
    List<String> findDistinctSourcesByUserId(@Param("userId") UUID userId);

    // 5.3. Danh sách Years (sắp xếp mới nhất -> cũ nhất)
    @Query("""
        SELECT DISTINCT b.publicationYear
        FROM UserBookmark b
        WHERE b.userId = :userId
          AND b.publicationYear IS NOT NULL
        ORDER BY b.publicationYear DESC
    """)
    List<Integer> findDistinctYearsByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT DISTINCT b.authorsSnapshot
        FROM UserBookmark b
        WHERE b.userId = :userId
            AND b.authorsSnapshot IS NOT NULL
            AND b.authorsSnapshot <> ''
        ORDER BY b.authorsSnapshot ASC
    """)
    List<String>  findDistinctAuthorsByUserId(@Param("userId") UUID userId);

    // ==========================================
    // ENDPOINT 6: UPDATE NOTE (Cần lấy Bookmark đảm bảo thuộc về user)
    // ==========================================
    Optional<UserBookmark> findByIdAndUserId(UUID id, UUID userId);


    // ==========================================
    // ENDPOINT 7 & 8: DELETE
    // ==========================================
    int deleteByIdAndUserId(UUID id, UUID userId);

    int deleteByUserIdAndOpenAlexId(UUID userId, String openAlexId);


    List<UserBookmark> findByUserIdAndOpenAlexIdIn(UUID userId, List<String> openAlexIds);
}