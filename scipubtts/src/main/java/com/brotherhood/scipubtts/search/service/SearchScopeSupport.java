package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.SearchEntityType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SearchScopeSupport {

    private final OpenAlexMapReader openAlexMapReader;

    public SearchScopeSupport(OpenAlexMapReader openAlexMapReader) {
        this.openAlexMapReader = openAlexMapReader;
    }

    public String getDirectFilter(SearchEntityType entityType) {
        if (SearchEntityType.WORKS.equals(entityType)) {
            return SearchConstants.WORKS_SCOPE_FILTER;
        }

        if (SearchEntityType.TOPICS.equals(entityType)) {
            return SearchConstants.TOPICS_SCOPE_FILTER;
        }

        return null;
    }

    public boolean requiresTopicProfileScope(SearchEntityType entityType) {
        return SearchEntityType.AUTHORS.equals(entityType);
    }

    public boolean matchesScopedTopicProfile(Map<String, Object> rawEntity) {
        List<Map<String, Object>> topics = openAlexMapReader.getMapList(rawEntity, "topics");

        for (Map<String, Object> topic : topics) {
            if (matchesScopedTopic(topic)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesScopedTopic(Map<String, Object> topic) {
        Map<String, Object> field = openAlexMapReader.getMap(topic, "field");
        Map<String, Object> domain = openAlexMapReader.getMap(topic, "domain");
        String fieldId = openAlexMapReader.getString(field, "id");
        String domainId = openAlexMapReader.getString(domain, "id");
        String normalizedFieldId = openAlexMapReader.sanitizeDisplayText(fieldId);
        String normalizedDomainId = openAlexMapReader.sanitizeDisplayText(domainId);

        if (normalizedFieldId == null || normalizedDomainId == null) {
            return false;
        }

        return normalizedDomainId.endsWith("/" + SearchConstants.SCOPED_DOMAIN_ID)
                && (
                normalizedFieldId.endsWith("/17")
                        || normalizedFieldId.endsWith("/22")
        );
    }
}
