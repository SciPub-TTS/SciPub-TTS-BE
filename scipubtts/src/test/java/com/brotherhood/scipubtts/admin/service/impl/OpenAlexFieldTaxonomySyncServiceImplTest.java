package com.brotherhood.scipubtts.admin.service.impl;

import com.brotherhood.scipubtts.admin.entity.OpenAlexSubfield;
import com.brotherhood.scipubtts.admin.entity.OpenAlexTopic;
import com.brotherhood.scipubtts.admin.repository.OpenAlexSubfieldRepository;
import com.brotherhood.scipubtts.admin.repository.OpenAlexTopicRepository;
import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenAlexFieldTaxonomySyncServiceImplTest {

    private OpenAlexClient openAlexClient;
    private OpenAlexSubfieldRepository openAlexSubfieldRepository;
    private OpenAlexTopicRepository openAlexTopicRepository;
    private OpenAlexFieldTaxonomySyncServiceImpl syncService;

    @BeforeEach
    void setUp() {
        openAlexClient = mock(OpenAlexClient.class);
        openAlexSubfieldRepository = mock(OpenAlexSubfieldRepository.class);
        openAlexTopicRepository = mock(OpenAlexTopicRepository.class);
        syncService = new OpenAlexFieldTaxonomySyncServiceImpl(
                openAlexClient,
                openAlexSubfieldRepository,
                openAlexTopicRepository
        );
    }

    @Test
    void syncFieldTaxonomyFetchesCursorPagesAndSkipsInvalidItems() {
        when(openAlexClient.get(eq("/subfields"), anyMap()))
                .thenReturn(
                        response(
                                List.of(
                                        item("https://openalex.org/subfields/1", "Artificial Intelligence"),
                                        Map.of("id", "https://openalex.org/subfields/missing-name")
                                ),
                                "next-subfield-cursor"
                        ),
                        response(
                                List.of(item("https://openalex.org/subfields/2", "Machine Learning")),
                                null
                        )
                );
        when(openAlexClient.get(eq("/topics"), anyMap()))
                .thenReturn(response(
                        List.of(
                                item("https://openalex.org/topics/1", "Large Language Models"),
                                Map.of("display_name", "Missing Id")
                        ),
                        null
                ));
        when(openAlexSubfieldRepository.findByOpenAlexId("https://openalex.org/subfields/1"))
                .thenReturn(Optional.empty());
        when(openAlexSubfieldRepository.findByOpenAlexId("https://openalex.org/subfields/2"))
                .thenReturn(Optional.empty());
        when(openAlexTopicRepository.findByOpenAlexId("https://openalex.org/topics/1"))
                .thenReturn(Optional.empty());

        syncService.syncFieldTaxonomy();

        ArgumentCaptor<OpenAlexSubfield> subfieldCaptor = ArgumentCaptor.forClass(OpenAlexSubfield.class);
        ArgumentCaptor<OpenAlexTopic> topicCaptor = ArgumentCaptor.forClass(OpenAlexTopic.class);
        verify(openAlexSubfieldRepository, org.mockito.Mockito.times(2)).save(subfieldCaptor.capture());
        verify(openAlexTopicRepository).save(topicCaptor.capture());

        assertThat(subfieldCaptor.getAllValues())
                .extracting(OpenAlexSubfield::getOpenAlexId, OpenAlexSubfield::getDisplayName)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(
                                "https://openalex.org/subfields/1",
                                "Artificial Intelligence"
                        ),
                        org.assertj.core.groups.Tuple.tuple(
                                "https://openalex.org/subfields/2",
                                "Machine Learning"
                        )
                );
        assertThat(topicCaptor.getValue().getOpenAlexId()).isEqualTo("https://openalex.org/topics/1");
        assertThat(topicCaptor.getValue().getDisplayName()).isEqualTo("Large Language Models");
        verify(openAlexClient).get(eq("/subfields"), org.mockito.ArgumentMatchers.argThat(params ->
                "field.id:17|22".equals(params.get("filter"))
                        && "id,display_name".equals(params.get("select"))
                        && "200".equals(params.get("per_page"))
                        && "*".equals(params.get("cursor"))
        ));
    }

    private Map<String, Object> response(List<Map<String, Object>> results, String nextCursor) {
        Map<String, Object> meta = nextCursor == null
                ? Map.of()
                : Map.of("next_cursor", nextCursor);
        return Map.of(
                "results", results,
                "meta", meta
        );
    }

    private Map<String, Object> item(String id, String displayName) {
        return Map.of(
                "id", id,
                "display_name", displayName
        );
    }
}
