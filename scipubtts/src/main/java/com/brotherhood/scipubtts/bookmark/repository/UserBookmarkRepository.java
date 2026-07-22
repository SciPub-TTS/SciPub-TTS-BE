package com.brotherhood.scipubtts.bookmark.repository;

import com.brotherhood.scipubtts.bookmark.entity.UserBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserBookmarkRepository extends JpaRepository<UserBookmark, UUID> {

    Optional<UserBookmark> findByUserIdAndOpenAlexId(UUID userId, String openAlexId);

    @Query("""
            SELECT b
            FROM UserBookmark b
            WHERE b.userId = :userId
              AND (:collectionId IS NULL OR EXISTS (
                  SELECT 1
                  FROM CollectionBookmark cb
                  JOIN BookmarkCollection c ON c.id = cb.collectionId
                  WHERE cb.bookmarkId = b.id
                    AND c.userId = :userId
                    AND c.id = :collectionId
              ))
              AND (
                  :keyword IS NULL OR :keyword = ''
                  OR LOWER(b.titleSnapshot) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(b.authorsSnapshot) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(b.workTypeSnapshot) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(b.sourceSnapshot) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(b.topicSnapshot) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<UserBookmark> searchMyBookmarks(
            @Param("userId") UUID userId,
            @Param("collectionId") UUID collectionId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countByUserId(UUID userId);

    Optional<UserBookmark> findByIdAndUserId(UUID id, UUID userId);

    int deleteByIdAndUserId(UUID id, UUID userId);

    void deleteByUserIdAndOpenAlexId(UUID userId, String openAlexId);

    List<UserBookmark> findByUserIdAndOpenAlexIdIn(UUID userId, List<String> openAlexIds);

    long countByUserIdAndIdIn(UUID userId, Collection<UUID> ids);

}
