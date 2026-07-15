package com.brotherhood.scipubtts.admin.service.impl;

import com.brotherhood.scipubtts.admin.entity.OpenAlexSubfield;
import com.brotherhood.scipubtts.admin.entity.OpenAlexTopic;
import com.brotherhood.scipubtts.admin.repository.OpenAlexSubfieldRepository;
import com.brotherhood.scipubtts.admin.repository.OpenAlexTopicRepository;
import com.brotherhood.scipubtts.admin.service.OpenAlexFieldTaxonomySyncService;
import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.common.openalex.OpenAlexCursorSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAlexFieldTaxonomySyncServiceImpl implements OpenAlexFieldTaxonomySyncService {

    private static final String FIELD_FILTER = "field.id:17|22";
    private static final String SELECT_FIELDS = "id,display_name";

    private final OpenAlexClient openAlexClient;
    private final OpenAlexSubfieldRepository openAlexSubfieldRepository;
    private final OpenAlexTopicRepository openAlexTopicRepository;

    @Override
    public void syncFieldTaxonomy() {
        log.info("Starting OpenAlex field taxonomy sync for fields 17 and 22");
        syncEndpoint("/subfields", this::upsertSubfield);
        syncEndpoint("/topics", this::upsertTopic);
        log.info("Finished OpenAlex field taxonomy sync for fields 17 and 22");
    }

    private void syncEndpoint(String path, BiConsumer<String, String> upsert) {
        String cursor = OpenAlexCursorSupport.INITIAL_CURSOR;

        while (OpenAlexCursorSupport.hasNextCursor(cursor)) {
            Map<String, Object> response = openAlexClient.get(path, queryParams(cursor));
            readResults(response).forEach(item -> readText(item, "id")
                    .flatMap(id -> readText(item, "display_name")
                            .map(displayName -> new TaxonomyItem(id, displayName)))
                    .ifPresent(taxonomyItem -> upsert.accept(
                            taxonomyItem.openAlexId(),
                            taxonomyItem.displayName()
                    )));

            cursor = readNextCursor(response).orElse(null);
        }
    }

    private Map<String, String> queryParams(String cursor) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("filter", FIELD_FILTER);
        params.put("select", SELECT_FIELDS);
        params.put("per_page", String.valueOf(OpenAlexCursorSupport.MAX_PER_PAGE));
        params.put("cursor", cursor);
        return params;
    }

    private List<Map<String, Object>> readResults(Map<String, Object> response) {
        Object results = response.get("results");
        if (!(results instanceof List<?> resultList)) {
            return List.of();
        }

        return resultList.stream()
                .filter(Map.class::isInstance)
                .map(item -> (Map<String, Object>) item)
                .toList();
    }

    private Optional<String> readNextCursor(Map<String, Object> response) {
        Object meta = response.get("meta");
        if (!(meta instanceof Map<?, ?> metaMap)) {
            return Optional.empty();
        }

        Object nextCursor = metaMap.get("next_cursor");
        if (nextCursor instanceof String cursor && StringUtils.hasText(cursor)) {
            return Optional.of(cursor);
        }

        return Optional.empty();
    }

    private Optional<String> readText(Map<String, Object> item, String key) {
        Object value = item.get(key);
        if (value instanceof String text && StringUtils.hasText(text)) {
            return Optional.of(text.trim());
        }

        return Optional.empty();
    }

    private void upsertSubfield(String openAlexId, String displayName) {
        OffsetDateTime now = OffsetDateTime.now();
        OpenAlexSubfield subfield = openAlexSubfieldRepository.findByOpenAlexId(openAlexId)
                .orElseGet(() -> {
                    OpenAlexSubfield created = new OpenAlexSubfield();
                    created.setOpenAlexId(openAlexId);
                    created.setCreatedAt(now);
                    return created;
                });

        subfield.setDisplayName(displayName);
        subfield.setUpdatedAt(now);
        subfield.setLastSyncedAt(now);
        openAlexSubfieldRepository.save(subfield);
    }

    private void upsertTopic(String openAlexId, String displayName) {
        OffsetDateTime now = OffsetDateTime.now();
        OpenAlexTopic topic = openAlexTopicRepository.findByOpenAlexId(openAlexId)
                .orElseGet(() -> {
                    OpenAlexTopic created = new OpenAlexTopic();
                    created.setOpenAlexId(openAlexId);
                    created.setCreatedAt(now);
                    return created;
                });

        topic.setDisplayName(displayName);
        topic.setUpdatedAt(now);
        topic.setLastSyncedAt(now);
        openAlexTopicRepository.save(topic);
    }

    private record TaxonomyItem(String openAlexId, String displayName) {
    }
}
