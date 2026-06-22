package com.brotherhood.scipubtts.search.service.impl;

import com.brotherhood.scipubtts.search.dto.HotKeywordResponse;
import com.brotherhood.scipubtts.search.repository.KeywordTrendReadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeywordTrendServiceImplTest {

    @Mock
    private KeywordTrendReadRepository keywordTrendReadRepository;

    private KeywordTrendServiceImpl keywordTrendService;

    @BeforeEach
    void setUp() {
        keywordTrendService = new KeywordTrendServiceImpl(keywordTrendReadRepository);
    }

    @Test
    void getWeeklyHotKeywordsUsesProvidedSnapshotAndClampsLimit() {
        LocalDate snapshotDate = LocalDate.of(2026, 6, 15);
        when(keywordTrendReadRepository.findTrendingKeywords(eq(snapshotDate), any(Pageable.class)))
                .thenReturn(List.of(
                        keywordRow("K123", "Machine Learning", "17", 120L, 340L)
                ));

        HotKeywordResponse response = keywordTrendService.getWeeklyHotKeywords(snapshotDate, 99);

        assertThat(response.snapshotDate()).isEqualTo(snapshotDate);
        assertThat(response.keywords()).hasSize(1);
        assertThat(response.keywords().getFirst().keywordId()).isEqualTo("K123");
        assertThat(response.keywords().getFirst().name()).isEqualTo("Machine Learning");
        assertThat(response.keywords().getFirst().fieldId()).isEqualTo(17);
        assertThat(response.keywords().getFirst().works()).isEqualTo(120L);
        assertThat(response.keywords().getFirst().citations()).isEqualTo(340L);

        verify(keywordTrendReadRepository).findTrendingKeywords(eq(snapshotDate), any(Pageable.class));
        verify(keywordTrendReadRepository, never()).findLatestSnapshotDate();
    }

    @Test
    void getWeeklyHotKeywordsFallsBackToLatestSnapshotWhenSnapshotIsMissing() {
        LocalDate latestSnapshot = LocalDate.of(2026, 6, 8);
        when(keywordTrendReadRepository.findLatestSnapshotDate())
                .thenReturn(latestSnapshot);
        when(keywordTrendReadRepository.findTrendingKeywords(eq(latestSnapshot), any(Pageable.class)))
                .thenReturn(List.of());

        HotKeywordResponse response = keywordTrendService.getWeeklyHotKeywords(null, 0);

        assertThat(response.snapshotDate()).isEqualTo(latestSnapshot);
        assertThat(response.keywords()).isEmpty();

        verify(keywordTrendReadRepository).findLatestSnapshotDate();
        verify(keywordTrendReadRepository).findTrendingKeywords(eq(latestSnapshot), any(Pageable.class));
    }

    private KeywordTrendReadRepository.KeywordTrendRow keywordRow(
            String keywordId,
            String keyword,
            String fieldId,
            Long worksCount,
            Long citedByCount
    ) {
        return new KeywordTrendReadRepository.KeywordTrendRow() {
            @Override
            public String getKeywordId() {
                return keywordId;
            }

            @Override
            public String getKeyword() {
                return keyword;
            }

            @Override
            public String getFieldId() {
                return fieldId;
            }

            @Override
            public Long getWorksCount() {
                return worksCount;
            }

            @Override
            public Long getCitedByCount() {
                return citedByCount;
            }
        };
    }
}
