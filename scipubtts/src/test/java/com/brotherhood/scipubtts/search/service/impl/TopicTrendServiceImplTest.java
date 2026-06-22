package com.brotherhood.scipubtts.search.service.impl;

import com.brotherhood.scipubtts.search.dto.HotTopicResponse;
import com.brotherhood.scipubtts.search.repository.TopicTrendReadRepository;
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
class TopicTrendServiceImplTest {

    @Mock
    private TopicTrendReadRepository topicTrendReadRepository;

    private TopicTrendServiceImpl topicTrendService;

    @BeforeEach
    void setUp() {
        topicTrendService = new TopicTrendServiceImpl(topicTrendReadRepository);
    }

    @Test
    void getWeeklyHotTopicsUsesProvidedSnapshotAndClampsLimit() {
        LocalDate snapshotDate = LocalDate.of(2026, 6, 15);
        when(topicTrendReadRepository.findTrendingTopics(eq(snapshotDate), any(Pageable.class)))
                .thenReturn(List.of(
                        topicRow("T456", "Neural Interfaces", 22, 88L, 144L)
                ));

        HotTopicResponse response = topicTrendService.getWeeklyHotTopics(snapshotDate, 42);

        assertThat(response.snapshotDate()).isEqualTo(snapshotDate);
        assertThat(response.topics()).hasSize(1);
        assertThat(response.topics().getFirst().topicId()).isEqualTo("T456");
        assertThat(response.topics().getFirst().name()).isEqualTo("Neural Interfaces");
        assertThat(response.topics().getFirst().fieldId()).isEqualTo(22);
        assertThat(response.topics().getFirst().works()).isEqualTo(88L);
        assertThat(response.topics().getFirst().citations()).isEqualTo(144L);

        verify(topicTrendReadRepository).findTrendingTopics(eq(snapshotDate), any(Pageable.class));
        verify(topicTrendReadRepository, never()).findLatestSnapshotDate();
    }

    @Test
    void getWeeklyHotTopicsFallsBackToLatestSnapshotWhenSnapshotIsMissing() {
        LocalDate latestSnapshot = LocalDate.of(2026, 6, 8);
        when(topicTrendReadRepository.findLatestSnapshotDate())
                .thenReturn(latestSnapshot);
        when(topicTrendReadRepository.findTrendingTopics(eq(latestSnapshot), any(Pageable.class)))
                .thenReturn(List.of());

        HotTopicResponse response = topicTrendService.getWeeklyHotTopics(null, -3);

        assertThat(response.snapshotDate()).isEqualTo(latestSnapshot);
        assertThat(response.topics()).isEmpty();

        verify(topicTrendReadRepository).findLatestSnapshotDate();
        verify(topicTrendReadRepository).findTrendingTopics(eq(latestSnapshot), any(Pageable.class));
    }

    private TopicTrendReadRepository.TopicTrendRow topicRow(
            String topicId,
            String name,
            Integer fieldId,
            Long works,
            Long citations
    ) {
        return new TopicTrendReadRepository.TopicTrendRow() {
            @Override
            public String getTopicId() {
                return topicId;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Integer getFieldId() {
                return fieldId;
            }

            @Override
            public Long getWorks() {
                return works;
            }

            @Override
            public Long getCitations() {
                return citations;
            }
        };
    }
}
