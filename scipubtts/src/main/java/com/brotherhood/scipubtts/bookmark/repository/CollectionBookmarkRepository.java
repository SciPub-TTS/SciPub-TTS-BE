package com.brotherhood.scipubtts.bookmark.repository;

import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkCollectionMembershipRow;
import com.brotherhood.scipubtts.bookmark.entity.CollectionBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface CollectionBookmarkRepository extends JpaRepository<CollectionBookmark, UUID> {

    int deleteByCollectionIdAndBookmarkId(UUID collectionId, UUID bookmarkId);

    List<CollectionBookmark> findByCollectionId(UUID collectionId);

    List<CollectionBookmark> findByCollectionIdAndBookmarkIdIn(UUID collectionId, Collection<UUID> bookmarkIds);

    @Query("""
            SELECT cb.bookmarkId
            FROM CollectionBookmark cb
            WHERE cb.collectionId = :collectionId
              AND cb.bookmarkId IN :bookmarkIds
            """)
    Set<UUID> findBookmarkIdsByCollectionIdAndBookmarkIdIn(
            @Param("collectionId") UUID collectionId,
            @Param("bookmarkIds") Collection<UUID> bookmarkIds
    );

    long countByCollectionId(UUID collectionId);

    @Query("""
            SELECT new com.brotherhood.scipubtts.bookmark.dto.response.BookmarkCollectionMembershipRow(
                cb.bookmarkId,
                c.id,
                c.name
            )
            FROM CollectionBookmark cb
            JOIN BookmarkCollection c ON c.id = cb.collectionId
            WHERE c.userId = :userId
              AND cb.bookmarkId IN :bookmarkIds
            ORDER BY cb.bookmarkId ASC, cb.createdAt ASC, c.createdAt ASC
            """)
    List<BookmarkCollectionMembershipRow> findMembershipRowsByUserIdAndBookmarkIds(
            @Param("userId") UUID userId,
            @Param("bookmarkIds") Collection<UUID> bookmarkIds
    );
}
