package com.brotherhood.scipubtts.bookmark.repository;

import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkCollectionResponse;
import com.brotherhood.scipubtts.bookmark.entity.BookmarkCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookmarkCollectionRepository extends JpaRepository<BookmarkCollection, UUID> {

    boolean existsByUserIdAndNameIgnoreCase(UUID userId, String name);

    Optional<BookmarkCollection> findByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("""
            DELETE FROM BookmarkCollection c
            WHERE c.id = :id
              AND c.userId = :userId
            """)
    int deleteOwnedCollectionById(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("""
            SELECT new com.brotherhood.scipubtts.bookmark.dto.response.BookmarkCollectionResponse(
                c.id,
                c.name,
                COUNT(cb.id)
            )
            FROM BookmarkCollection c
            LEFT JOIN CollectionBookmark cb ON cb.collectionId = c.id
            WHERE c.userId = :userId
            GROUP BY c.id, c.name, c.createdAt
            ORDER BY LOWER(c.name) ASC, c.createdAt ASC
            """)
    List<BookmarkCollectionResponse> findCollectionResponsesByUserId(@Param("userId") UUID userId);
}
