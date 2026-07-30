package com.brotherhood.scipubtts.detail.works.service;

import com.brotherhood.scipubtts.detail.works.dto.response.DetailWorkResponse;
import com.brotherhood.scipubtts.search.dto.response.SearchWorksResponse;
import com.brotherhood.scipubtts.search.service.SearchQuerySupport;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DetailWorkResponseMapper {

    private final SearchQuerySupport searchQuerySupport;

    public DetailWorkResponseMapper(SearchQuerySupport searchQuerySupport) {
        this.searchQuerySupport = searchQuerySupport;
    }

    public List<DetailWorkResponse> mapWorkItems(List<SearchWorksResponse.WorkItem> works) {
        List<DetailWorkResponse> items = new ArrayList<>();

        for (SearchWorksResponse.WorkItem work : safeList(works)) {
            items.add(mapWorkItem(work));
        }

        return items;
    }

    private DetailWorkResponse mapWorkItem(SearchWorksResponse.WorkItem work) {
        String source = fallbackText(work.sourceName(), "Unknown source");
        DetailWorkResponse.EntityRef topicRef = mapEntityRef(work.topicRef());
        String topic = topicRef != null
                ? topicRef.name()
                : fallbackText(work.topic(), source);
        String subField = fallbackText(work.subFieldName(), "Unknown subfield");
        String abstractText = fallbackText(
                work.abstractText(),
                "OpenAlex result from " + source + "."
        );
        List<DetailWorkResponse.EntityRef> authorRefs = mapAuthorRefs(work);
        List<String> authors = authorRefs.isEmpty()
                ? mapAuthorNames(work.authors())
                : authorRefs.stream().map(DetailWorkResponse.EntityRef::name).toList();

        return new DetailWorkResponse(
                extractLastSegment(work.id()),
                "works",
                fallbackText(work.title(), "Untitled"),
                authors,
                authorRefs,
                source,
                Math.max(work.citedByCount() == null ? 0 : work.citedByCount(), 0),
                normalizePublicationYear(work.publicationYear()),
                abstractText,
                abstractText,
                normalizeDoiValue(work.doi()),
                work.pdfUrl(),
                buildKeywords(work.keywords(), subField, topic),
                normalizeTypeLabel(work.type()),
                topic,
                topicRef,
                subField,
                0,
                false
        );
    }

    private List<DetailWorkResponse.EntityRef> mapAuthorRefs(SearchWorksResponse.WorkItem work) {
        List<DetailWorkResponse.EntityRef> authorRefs = new ArrayList<>();

        for (SearchWorksResponse.EntityRef authorRef : safeList(work.authorRefs())) {
            if (!StringUtils.hasText(authorRef.displayName())) {
                continue;
            }

            authorRefs.add(new DetailWorkResponse.EntityRef(
                    normalizeEntityId(authorRef.id()),
                    authorRef.displayName().trim()
            ));
        }

        if (!authorRefs.isEmpty()) {
            return authorRefs;
        }

        for (String authorName : mapAuthorNames(work.authors())) {
            authorRefs.add(new DetailWorkResponse.EntityRef(null, authorName));
        }

        return authorRefs;
    }

    private List<String> mapAuthorNames(List<String> authorNames) {
        List<String> names = new ArrayList<>();

        for (String authorName : safeList(authorNames)) {
            if (StringUtils.hasText(authorName)) {
                names.add(authorName.trim());
            }
        }

        return names;
    }

    private DetailWorkResponse.EntityRef mapEntityRef(SearchWorksResponse.EntityRef entityRef) {
        if (entityRef == null || !StringUtils.hasText(entityRef.displayName())) {
            return null;
        }

        return new DetailWorkResponse.EntityRef(
                normalizeEntityId(entityRef.id()),
                entityRef.displayName().trim()
        );
    }

    private List<String> buildKeywords(
            List<String> rawKeywords,
            String subField,
            String topic
    ) {
        LinkedHashSet<String> keywords = new LinkedHashSet<>();

        for (String keyword : safeList(rawKeywords)) {
            if (StringUtils.hasText(keyword)) {
                keywords.add(keyword.trim());
            }

            if (keywords.size() == 8) {
                return new ArrayList<>(keywords);
            }
        }

        addKeywordFallback(keywords, subField);
        addKeywordFallback(keywords, topic);

        return new ArrayList<>(keywords);
    }

    private void addKeywordFallback(LinkedHashSet<String> keywords, String value) {
        if (keywords.size() < 8 && StringUtils.hasText(value)) {
            keywords.add(value.trim());
        }
    }

    private int normalizePublicationYear(Integer publicationYear) {
        int currentYear = Year.now().getValue();

        if (publicationYear == null || publicationYear <= 0) {
            return currentYear;
        }

        return Math.min(publicationYear, currentYear);
    }

    private String normalizeTypeLabel(String type) {
        String normalizedType = fallbackText(type, "Work");

        return Arrays.stream(normalizedType.split("-"))
                .filter(StringUtils::hasText)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(" "));
    }

    private String normalizeDoiValue(String doi) {
        return fallbackText(doi).replaceFirst("(?i)^https?://", "");
    }

    private String normalizeEntityId(String value) {
        String normalizedValue = extractLastSegment(value);
        return StringUtils.hasText(normalizedValue) ? normalizedValue : null;
    }

    private String extractLastSegment(String value) {
        return searchQuerySupport.extractLastSegment(value);
    }

    private String fallbackText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }

        return "";
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}
