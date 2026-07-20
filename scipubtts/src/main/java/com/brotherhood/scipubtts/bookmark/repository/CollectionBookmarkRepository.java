package com.brotherhood.scipubtts.bookmark.repository;

import com.brotherhood.scipubtts.bookmark.entity.CollectionBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface CollectionBookmarkRepository extends JpaRepository<CollectionBookmark, UUID> {

    @Modifying
    @Query("""
            DELETE FROM CollectionBookmark cb
            WHERE cb.collectionId = :collectionId
              AND cb.bookmarkId = :bookmarkId
            """)
    int deleteByCollectionIdAndBookmarkId(
            @Param("collectionId") UUID collectionId,
            @Param("bookmarkId") UUID bookmarkId
    );

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

    @Query("""
            SELECT new com.brotherhood.scipubtts.bookmark.repository.BookmarkCollectionMembershipRow(
                cb.bookmarkId,
                c.id,
                c.name,
                COUNT(allItems.id)
            )
            FROM CollectionBookmark cb
            JOIN BookmarkCollection c ON c.id = cb.collectionId
            LEFT JOIN CollectionBookmark allItems ON allItems.collectionId = c.id
            WHERE c.userId = :userId
              AND cb.bookmarkId IN :bookmarkIds
            GROUP BY cb.bookmarkId, c.id, c.name, cb.createdAt, c.createdAt
            ORDER BY cb.bookmarkId ASC, cb.createdAt ASC, c.createdAt ASC
            """)
    List<BookmarkCollectionMembershipRow> findMembershipRowsByUserIdAndBookmarkIds(
            @Param("userId") UUID userId,
            @Param("bookmarkIds") Collection<UUID> bookmarkIds
    );
}
