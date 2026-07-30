package com.brotherhood.scipubtts.journalDaily.service.impl;

import com.brotherhood.scipubtts.journalDaily.dto.request.JournalDailyResultItem;
import com.brotherhood.scipubtts.journalDaily.entity.JournalDailyArticle;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
public class JournalDailyArticleMapper {

    /**
     * Maps a single JournalDailyResultItem to a JournalDailyArticle entity.
     * Returns null if essential fields (id, title, webUrl, publishedAt) are missing or invalid.
     */
    public JournalDailyArticle toEntity(JournalDailyResultItem item) {
        if (item == null
                || !StringUtils.hasText(item.id())
                || !StringUtils.hasText(item.webTitle())
                || !StringUtils.hasText(item.webUrl())) {
            log.warn("[Guardian] Skipping article with missing essential data: {}", item);
            return null;
        }

        // 1. Parse publishedAt — SKIP article if missing or invalid since DB column is NOT NULL
        OffsetDateTime publishedAt = parseDate(item.webPublicationDate(), item.id());
        if (publishedAt == null) {
            log.warn("[Guardian] Skipping article '{}' due to missing or invalid webPublicationDate", item.id());
            return null;
        }

        JournalDailyResultItem.GuardianFields fields = item.fields();

        // 2. Sanitize trailText (strips HTML tags using Jsoup)
        String rawSummary = fields != null ? fields.trailText() : null;
        String cleanSummary = sanitizeHtml(rawSummary);

        JournalDailyArticle article = JournalDailyArticle.builder()
                .externalId(item.id())
                .title(truncate(item.webTitle(), 500))
                .summary(cleanSummary)
                .author(fields != null ? truncate(fields.byline(), 255) : null)
                .thumbnailUrl(fields != null ? truncate(fields.thumbnail(), 1000) : null)
                .sourceUrl(truncate(item.webUrl(), 1000))
                .publishedAt(publishedAt)
                .category(truncate(item.sectionName(), 100))
                .build();

        // 3. Add tags — filter out empty/blank tags
        if (item.tags() != null) {
            item.tags().stream()
                    .filter(tag -> StringUtils.hasText(tag.webTitle()))
                    .map(tag -> truncate(tag.webTitle(), 255))
                    .forEach(article::addTag);
        }

        return article;
    }

    /**
     * Parses ISO-8601 string (e.g., "2026-07-29T06:00:12Z") to OffsetDateTime.
     * Returns null if dateStr is blank or invalid.
     */
    private OffsetDateTime parseDate(String dateStr, String articleId) {
        if (!StringUtils.hasText(dateStr)) {
            log.warn("[Guardian] Article '{}' is missing webPublicationDate", articleId);
            return null;
        }
        try {
            return OffsetDateTime.parse(dateStr);
        } catch (DateTimeParseException e) {
            log.warn("[Guardian] Failed to parse date '{}' for article '{}'", dateStr, articleId);
            return null;
        }
    }

    /**
     * Sanitizes HTML content in summary using Jsoup (Safelist.none() leaves only plain text).
     */
    private String sanitizeHtml(String rawHtml) {
        if (!StringUtils.hasText(rawHtml)) {
            return null;
        }
        return Jsoup.clean(rawHtml, Safelist.none());
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}