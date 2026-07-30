package com.brotherhood.scipubtts.journalDaily.service.impl;

import com.brotherhood.scipubtts.journalDaily.dto.response.JournalDailyArticleResponse;
import com.brotherhood.scipubtts.journalDaily.entity.JournalDailyArticle;
import com.brotherhood.scipubtts.journalDaily.entity.JournalDailyArticleTag;
import com.brotherhood.scipubtts.journalDaily.repository.JournalDailyArticleRepository;
import com.brotherhood.scipubtts.journalDaily.service.JournalDailyArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalDailyArticleServiceImpl
        implements JournalDailyArticleService {

    private final JournalDailyArticleRepository articleRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<JournalDailyArticleResponse> searchByTitle(
            String keyword,
            Pageable pageable
    ) {
        Page<JournalDailyArticle> articles;

        if (StringUtils.hasText(keyword)) {
            articles = articleRepository.findByTitleContainingIgnoreCase(
                    keyword.trim(),
                    pageable
            );
        } else {
            articles = articleRepository.findAll(pageable);
        }

        return articles.map(this::toResponse);
    }

    private JournalDailyArticleResponse toResponse(
            JournalDailyArticle article
    ) {
        List<String> tagNames = article.getTags() == null
                ? List.of()
                : article.getTags().stream()
                .map(JournalDailyArticleTag::getTagName)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        return new JournalDailyArticleResponse(
                article.getId(),
                article.getExternalId(),
                article.getTitle(),
                article.getSummary(),
                article.getAuthor(),
                article.getThumbnailUrl(),
                article.getSourceUrl(),
                article.getPublishedAt(),
                article.getCategory(),
                tagNames
        );
    }
}