package com.brotherhood.scipubtts.journalDaily.repository;

import com.brotherhood.scipubtts.journalDaily.entity.JournalDailyArticle;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface JournalDailyArticleRepository extends JpaRepository<JournalDailyArticle, Long> {

    // Bỏ @EntityGraph ở các hàm phân trang
    Page<JournalDailyArticle> findByTitleContainingIgnoreCase(
            String title,
            Pageable pageable
    );

    /**
     * Kiểm tra bài nào trong danh sách external_id đã tồn tại trong DB.
     * CronJob dùng kết quả này để lọc ra chỉ bài chưa có → tránh duplicate.
     */
    @Query("SELECT a.externalId FROM JournalDailyArticle a WHERE a.externalId IN :ids")
    Set<String> findExistingExternalIds(@Param("ids") List<String> ids);

    boolean existsByExternalId(String externalId);



}
