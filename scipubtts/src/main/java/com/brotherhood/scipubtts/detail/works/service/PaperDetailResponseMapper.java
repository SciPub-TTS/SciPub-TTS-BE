package com.brotherhood.scipubtts.detail.works.service;

import com.brotherhood.scipubtts.detail.works.dto.response.PaperDetailResponse;
import com.brotherhood.scipubtts.detail.works.dto.response.WorkReferenceSummaryResponse;
import com.brotherhood.scipubtts.search.service.OpenAlexMapReader;
import com.brotherhood.scipubtts.search.service.SearchQuerySupport;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class PaperDetailResponseMapper {

    private static final DateTimeFormatter PUBLISHED_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US);

    private final OpenAlexMapReader openAlexMapReader;
    private final SearchQuerySupport searchQuerySupport;

    public PaperDetailResponseMapper(
            OpenAlexMapReader openAlexMapReader,
            SearchQuerySupport searchQuerySupport
    ) {
        this.openAlexMapReader = openAlexMapReader;
        this.searchQuerySupport = searchQuerySupport;
    }

    public PaperDetailResponse mapWorkDetail(
            Map<String, Object> work,
            List<WorkReferenceSummaryResponse> referencedWorks,
            List<WorkReferenceSummaryResponse> relatedWorks
    ) {
        List<PaperDetailResponse.AuthorItem> authors = mapAuthors(work);
        List<PaperDetailResponse.InstitutionItem> institutions = mapInstitutions(work);
        Map<String, Object> primaryLocation = openAlexMapReader.getMap(work, "primary_location");
        Map<String, Object> bestOaLocation = openAlexMapReader.getMap(work, "best_oa_location");
        Map<String, Object> primaryLocationSource = openAlexMapReader.getMap(primaryLocation, "source");
        String sourceName = fallbackText(
                openAlexMapReader.getString(primaryLocationSource, "display_name"),
                "Unknown source"
        );
        String workType = formatTypeLabel(
                fallbackText(
                        openAlexMapReader.getString(work, "type"),
                        openAlexMapReader.getString(primaryLocation, "raw_type"),
                        "work"
                )
        );

        return new PaperDetailResponse(
                fallbackText(buildAbstractText(work), "No abstract available."),
                buildAccessItems(work, sourceName),
                authors,
                buildAwardLabels(openAlexMapReader.getMapListFromObject(work.get("awards"))),
                openAlexMapReader.getLong(work, "cited_by_count", 0L),
                trimToNull(openAlexMapReader.getString(work, "doi")),
                normalizeIdentifierLabel(openAlexMapReader.getString(work, "doi")),
                buildHeaderBadges(work, workType),
                mapStringList(work.get("indexed_in")),
                institutions,
                mapCountries(work),
                buildMetrics(work, authors.size(), institutions.size()),
                mapKeywords(work),
                formatLanguageLabel(openAlexMapReader.getString(work, "language")),
                extractLastSegment(openAlexMapReader.getString(work, "id")),
                resolveWorkPdfUrl(work),
                openAlexMapReader.getInteger(work, "publication_year"),
                formatPublishedLabel(
                        openAlexMapReader.getInteger(work, "publication_year"),
                        openAlexMapReader.getString(work, "publication_date")
                ),
                buildQuickLinks(work),
                mapWorkLinks(referencedWorks, work.get("referenced_works")),
                mapWorkLinks(relatedWorks, work.get("related_works")),
                mapNamedEntityToDetailRef(
                        firstNonEmptyMap(primaryLocationSource, openAlexMapReader.getMap(bestOaLocation, "source")),
                        "source"
                ),
                trimToNull(openAlexMapReader.getString(primaryLocationSource, "host_organization_name")),
                sourceName,
                formatTypeLabel(fallbackText(openAlexMapReader.getString(primaryLocationSource, "type"), "unknown")),
                fallbackText(openAlexMapReader.getString(work, "title"), "Untitled work"),
                mapTopics(work),
                workType
        );
    }

    private String buildAbstractText(Map<String, Object> work) {
        return openAlexMapReader.deriveAbstractText(
                openAlexMapReader.getMap(work, "abstract_inverted_index")
        );
    }

    private List<PaperDetailResponse.SummaryItem> buildAccessItems(
            Map<String, Object> work,
            String sourceName
    ) {
        List<PaperDetailResponse.SummaryItem> items = new ArrayList<>();
        Map<String, Object> openAccess = openAlexMapReader.getMap(work, "open_access");
        Map<String, Object> bestOaLocation = openAlexMapReader.getMap(work, "best_oa_location");
        Map<String, Object> hasContent = openAlexMapReader.getMap(work, "has_content");
        Map<String, Object> apcList = openAlexMapReader.getMap(work, "apc_list");
        Map<String, Object> apcPaid = openAlexMapReader.getMap(work, "apc_paid");

        addSummaryItem(items, "OA status", formatOpenAccessStatus(openAlexMapReader.getString(openAccess, "oa_status")));
        addSummaryItem(items, "Best OA source", sourceName);
        addSummaryItem(items, "License", formatLicenseLabel(openAlexMapReader.getString(bestOaLocation, "license")));
        addSummaryItem(items, "Version", formatTypeLabel(openAlexMapReader.getString(bestOaLocation, "version")));
        addSummaryItem(
                items,
                "Full text",
                formatAvailabilityLabel(Boolean.TRUE.equals(openAlexMapReader.getBoolean(openAccess, "any_repository_has_fulltext")))
        );
        addSummaryItem(
                items,
                "PDF",
                formatAvailabilityLabel(
                        Boolean.TRUE.equals(openAlexMapReader.getBoolean(hasContent, "pdf"))
                                || StringUtils.hasText(openAlexMapReader.getString(bestOaLocation, "pdf_url"))
                )
        );
        addSummaryItem(
                items,
                "TEI XML",
                formatAvailabilityLabel(Boolean.TRUE.equals(openAlexMapReader.getBoolean(hasContent, "grobid_xml")))
        );
        addSummaryItem(
                items,
                "APC list",
                fallbackText(
                        formatCurrency(openAlexMapReader.getInteger(apcList, "value"), openAlexMapReader.getString(apcList, "currency")),
                        "Unavailable"
                )
        );
        addSummaryItem(
                items,
                "APC paid",
                fallbackText(
                        formatCurrency(openAlexMapReader.getInteger(apcPaid, "value"), openAlexMapReader.getString(apcPaid, "currency")),
                        "Unavailable"
                )
        );
        addSummaryItem(items, "Retracted", Boolean.TRUE.equals(openAlexMapReader.getBoolean(work, "is_retracted")) ? "Yes" : "No");

        return items;
    }

    private List<String> buildAwardLabels(List<Map<String, Object>> awards) {
        LinkedHashSet<String> uniqueAwards = new LinkedHashSet<>();

        for (Map<String, Object> award : awards) {
            String label = fallbackText(
                    openAlexMapReader.getString(award, "display_name"),
                    buildAwardFallbackLabel(award),
                    openAlexMapReader.getString(award, "name"),
                    openAlexMapReader.getString(award, "title")
            );

            if (StringUtils.hasText(label)) {
                uniqueAwards.add(label);
            }
        }

        return new ArrayList<>(uniqueAwards);
    }

    private String buildAwardFallbackLabel(Map<String, Object> award) {
        String funderName = trimToEmpty(openAlexMapReader.getString(award, "funder_display_name"));
        String awardId = trimToEmpty(openAlexMapReader.getString(award, "funder_award_id"));

        if (StringUtils.hasText(funderName) && StringUtils.hasText(awardId)) {
            return funderName + " (" + awardId + ")";
        }

        return fallbackText(funderName, awardId);
    }

    private List<PaperDetailResponse.AuthorItem> mapAuthors(Map<String, Object> work) {
        List<PaperDetailResponse.AuthorItem> authors = new ArrayList<>();
        List<Map<String, Object>> authorships = openAlexMapReader.getMapList(work, "authorships");

        for (int index = 0; index < authorships.size(); index++) {
            Map<String, Object> authorship = authorships.get(index);
            Map<String, Object> author = openAlexMapReader.getMap(authorship, "author");
            String authorName = fallbackText(
                    openAlexMapReader.getString(author, "display_name"),
                    openAlexMapReader.getString(authorship, "raw_author_name")
            );

            if (!StringUtils.hasText(authorName)) {
                continue;
            }

            String authorIdentifier = fallbackText(
                    openAlexMapReader.getString(author, "id"),
                    "author-" + (index + 1) + "-" + authorName
            );

            authors.add(new PaperDetailResponse.AuthorItem(
                    extractLastSegment(authorIdentifier),
                    StringUtils.hasText(openAlexMapReader.getString(author, "id"))
                            ? extractLastSegment(openAlexMapReader.getString(author, "id"))
                            : null,
                    Boolean.TRUE.equals(openAlexMapReader.getBoolean(authorship, "is_corresponding")),
                    false,
                    authorName,
                    trimToNull(openAlexMapReader.getString(author, "orcid")),
                    trimToNull(openAlexMapReader.getString(authorship, "author_position"))
            ));
        }

        return authors;
    }

    private List<PaperDetailResponse.InstitutionItem> mapInstitutions(Map<String, Object> work) {
        Map<String, PaperDetailResponse.InstitutionItem> uniqueInstitutions = new LinkedHashMap<>();

        for (Map<String, Object> authorship : openAlexMapReader.getMapList(work, "authorships")) {
            for (Map<String, Object> institution : openAlexMapReader.getMapList(authorship, "institutions")) {
                String institutionId = openAlexMapReader.getString(institution, "id");
                String institutionName = trimToEmpty(openAlexMapReader.getString(institution, "display_name"));

                if (!StringUtils.hasText(institutionId) || !StringUtils.hasText(institutionName)) {
                    continue;
                }

                uniqueInstitutions.putIfAbsent(
                        institutionId,
                        new PaperDetailResponse.InstitutionItem(
                                trimToNull(formatCountryLabel(openAlexMapReader.getString(institution, "country_code"))),
                                trimToNull(openAlexMapReader.getString(institution, "country_code")),
                                extractLastSegment(institutionId),
                                institutionName,
                                trimToNull(openAlexMapReader.getString(institution, "type"))
                        )
                );
            }
        }

        return new ArrayList<>(uniqueInstitutions.values());
    }

    private List<PaperDetailResponse.CountryItem> mapCountries(Map<String, Object> work) {
        Map<String, PaperDetailResponse.CountryItem> uniqueCountries = new LinkedHashMap<>();

        for (Map<String, Object> authorship : openAlexMapReader.getMapList(work, "authorships")) {
            for (Object rawCountryCode : openAlexMapReader.getObjectList(authorship.get("countries"))) {
                addCountry(uniqueCountries, String.valueOf(rawCountryCode));
            }

            for (Map<String, Object> institution : openAlexMapReader.getMapList(authorship, "institutions")) {
                addCountry(uniqueCountries, openAlexMapReader.getString(institution, "country_code"));
            }
        }

        return new ArrayList<>(uniqueCountries.values());
    }

    private void addCountry(
            Map<String, PaperDetailResponse.CountryItem> uniqueCountries,
            String rawCountryCode
    ) {
        String countryCode = trimToEmpty(rawCountryCode).toUpperCase(Locale.ROOT);

        if (!StringUtils.hasText(countryCode) || uniqueCountries.containsKey(countryCode)) {
            return;
        }

        String countryName = formatCountryLabel(countryCode);
        if (!StringUtils.hasText(countryName)) {
            return;
        }

        uniqueCountries.put(countryCode, new PaperDetailResponse.CountryItem(countryCode, countryName));
    }

    private List<PaperDetailResponse.EntityRef> mapTopics(Map<String, Object> work) {
        Map<String, PaperDetailResponse.EntityRef> uniqueTopics = new LinkedHashMap<>();

        addTopic(uniqueTopics, openAlexMapReader.getMap(work, "primary_topic"));

        for (Map<String, Object> topic : openAlexMapReader.getMapList(work, "topics")) {
            addTopic(uniqueTopics, topic);
        }

        return uniqueTopics.values().stream().limit(8).toList();
    }

    private void addTopic(
            Map<String, PaperDetailResponse.EntityRef> uniqueTopics,
            Map<String, Object> topic
    ) {
        PaperDetailResponse.EntityRef topicRef = mapNamedEntityToDetailRef(topic, "topic");

        if (topicRef == null || uniqueTopics.containsKey(topicRef.id())) {
            return;
        }

        uniqueTopics.put(topicRef.id(), topicRef);
    }

    private PaperDetailResponse.EntityRef mapNamedEntityToDetailRef(
            Map<String, Object> entity,
            String type
    ) {
        String id = trimToEmpty(openAlexMapReader.getString(entity, "id"));
        String name = trimToEmpty(openAlexMapReader.getString(entity, "display_name"));

        if (!StringUtils.hasText(id) || !StringUtils.hasText(name)) {
            return null;
        }

        return new PaperDetailResponse.EntityRef(extractLastSegment(id), name, type);
    }

    private List<PaperDetailResponse.BadgeItem> buildHeaderBadges(
            Map<String, Object> work,
            String workType
    ) {
        List<PaperDetailResponse.BadgeItem> badges = new ArrayList<>();
        Map<String, Object> primaryTopic = openAlexMapReader.getMap(work, "primary_topic");
        Map<String, Object> subfield = openAlexMapReader.getMap(primaryTopic, "subfield");
        String subfieldName = trimToEmpty(openAlexMapReader.getString(subfield, "display_name"));
        String topicName = trimToEmpty(openAlexMapReader.getString(primaryTopic, "display_name"));

        if (StringUtils.hasText(subfieldName)) {
            badges.add(new PaperDetailResponse.BadgeItem(subfieldName, "accent", null, null));
        }

        if (StringUtils.hasText(topicName)) {
            badges.add(new PaperDetailResponse.BadgeItem(
                    topicName,
                    "topic",
                    extractLastSegment(openAlexMapReader.getString(primaryTopic, "id")),
                    "topic"
            ));
        }

        badges.add(new PaperDetailResponse.BadgeItem(workType, "default", null, null));
        return badges;
    }

    private List<PaperDetailResponse.MetricItem> buildMetrics(
            Map<String, Object> work,
            int authorCount,
            int institutionCount
    ) {
        List<PaperDetailResponse.MetricItem> items = new ArrayList<>();
        Map<String, Object> percentile = openAlexMapReader.getMap(work, "citation_normalized_percentile");

        addMetricItem(items, "FWCI", formatDecimalValue(openAlexMapReader.getDouble(work, "fwci", Double.NaN)));
        addMetricItem(items, "Citations", formatFullNumber(openAlexMapReader.getLong(work, "cited_by_count", 0L)));
        addMetricItem(items, "Citation percentile", formatCitationPercentile(openAlexMapReader.getDouble(percentile, "value", Double.NaN)));
        addMetricItem(items, "Referenced works", formatFullNumber(openAlexMapReader.getLong(work, "referenced_works_count", 0L)));
        addMetricItem(items, "Related works", formatFullNumber(openAlexMapReader.getObjectList(work.get("related_works")).size()));
        addMetricItem(items, "Locations", formatFullNumber(openAlexMapReader.getLong(work, "locations_count", 0L)));
        addMetricItem(items, "Authors", formatFullNumber(authorCount));
        addMetricItem(items, "Institutions", formatFullNumber(institutionCount));

        return items;
    }

    private List<PaperDetailResponse.QuickLinkItem> buildQuickLinks(Map<String, Object> work) {
        List<PaperDetailResponse.QuickLinkItem> quickLinks = new ArrayList<>();
        Set<String> seenLinks = new LinkedHashSet<>();
        Map<String, Object> ids = openAlexMapReader.getMap(work, "ids");
        Map<String, Object> primaryLocation = openAlexMapReader.getMap(work, "primary_location");
        Map<String, Object> bestOaLocation = openAlexMapReader.getMap(work, "best_oa_location");
        Map<String, Object> openAccess = openAlexMapReader.getMap(work, "open_access");

        addQuickLink(quickLinks, "OpenAlex", fallbackText(openAlexMapReader.getString(ids, "openalex"), openAlexMapReader.getString(work, "id")), seenLinks);
        addQuickLink(quickLinks, "PubMed", normalizePubmedUrl(openAlexMapReader.getString(ids, "pmid")), seenLinks);
        addQuickLink(
                quickLinks,
                "View Source",
                fallbackText(
                        openAlexMapReader.getString(primaryLocation, "landing_page_url"),
                        openAlexMapReader.getString(bestOaLocation, "landing_page_url")
                ),
                seenLinks
        );
        addQuickLink(quickLinks, "Open Access Link", openAlexMapReader.getString(openAccess, "oa_url"), seenLinks);

        return quickLinks;
    }

    private void addQuickLink(
            List<PaperDetailResponse.QuickLinkItem> quickLinks,
            String label,
            String href,
            Set<String> seenLinks
    ) {
        String normalizedHref = trimToEmpty(href);

        if (!StringUtils.hasText(normalizedHref)) {
            quickLinks.add(new PaperDetailResponse.QuickLinkItem(null, label, "Not available"));
            return;
        }

        String dedupeKey = normalizedHref.toLowerCase(Locale.ROOT);
        if (seenLinks.contains(dedupeKey)) {
            return;
        }

        seenLinks.add(dedupeKey);
        quickLinks.add(new PaperDetailResponse.QuickLinkItem(
                normalizedHref,
                label,
                formatHostnameLabel(normalizedHref)
        ));
    }

    private List<String> mapKeywords(Map<String, Object> work) {
        List<String> keywords = new ArrayList<>();

        for (Map<String, Object> keyword : openAlexMapReader.getMapList(work, "keywords")) {
            String keywordName = trimToEmpty(openAlexMapReader.getString(keyword, "display_name"));

            if (StringUtils.hasText(keywordName)) {
                keywords.add(keywordName);
            }

            if (keywords.size() == 8) {
                break;
            }
        }

        return keywords;
    }

    private List<PaperDetailResponse.WorkLinkItem> mapWorkLinks(
            List<WorkReferenceSummaryResponse> workReferences,
            Object fallbackWorkIds
    ) {
        List<PaperDetailResponse.WorkLinkItem> workLinks = new ArrayList<>();

        if (workReferences != null && !workReferences.isEmpty()) {
            for (WorkReferenceSummaryResponse workReference : workReferences) {
                String workId = extractLastSegment(workReference.id());
                if (!StringUtils.hasText(workId)) {
                    continue;
                }

                workLinks.add(new PaperDetailResponse.WorkLinkItem(
                        workId,
                        fallbackText(workReference.title(), normalizeOpenAlexWorkId(workId))
                ));
            }

            return dedupeWorkLinks(workLinks);
        }

        for (Object rawWorkId : openAlexMapReader.getObjectList(fallbackWorkIds)) {
            String workId = extractLastSegment(String.valueOf(rawWorkId));
            if (StringUtils.hasText(workId)) {
                workLinks.add(new PaperDetailResponse.WorkLinkItem(workId, normalizeOpenAlexWorkId(workId)));
            }
        }

        return dedupeWorkLinks(workLinks);
    }

    private List<PaperDetailResponse.WorkLinkItem> dedupeWorkLinks(
            List<PaperDetailResponse.WorkLinkItem> workLinks
    ) {
        Map<String, PaperDetailResponse.WorkLinkItem> uniqueLinks = new LinkedHashMap<>();

        for (PaperDetailResponse.WorkLinkItem workLink : workLinks) {
            String normalizedKey = normalizeWorkLinkLabel(workLink.label());

            if (StringUtils.hasText(normalizedKey) && !uniqueLinks.containsKey(normalizedKey)) {
                uniqueLinks.put(normalizedKey, workLink);
                continue;
            }

            if (!StringUtils.hasText(normalizedKey) && !uniqueLinks.containsKey(workLink.id())) {
                uniqueLinks.put(workLink.id(), workLink);
            }
        }

        return new ArrayList<>(uniqueLinks.values());
    }

    private String resolveWorkPdfUrl(Map<String, Object> work) {
        Map<String, Object> bestOaLocation = openAlexMapReader.getMap(work, "best_oa_location");
        Map<String, Object> primaryLocation = openAlexMapReader.getMap(work, "primary_location");
        Map<String, Object> contentUrls = openAlexMapReader.getMap(work, "content_urls");
        List<String> candidateUrls = List.of(
                openAlexMapReader.getString(bestOaLocation, "pdf_url"),
                openAlexMapReader.getString(primaryLocation, "pdf_url"),
                openAlexMapReader.getString(contentUrls, "pdf")
        );

        for (String candidateUrl : candidateUrls) {
            if (StringUtils.hasText(candidateUrl)) {
                return candidateUrl.trim();
            }
        }

        return null;
    }

    private void addSummaryItem(
            List<PaperDetailResponse.SummaryItem> items,
            String label,
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        items.add(new PaperDetailResponse.SummaryItem(label, value, null));
    }

    private void addMetricItem(
            List<PaperDetailResponse.MetricItem> items,
            String label,
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        items.add(new PaperDetailResponse.MetricItem(label, value));
    }

    private List<String> mapStringList(Object value) {
        List<String> items = new ArrayList<>();

        for (Object rawItem : openAlexMapReader.getObjectList(value)) {
            String item = trimToEmpty(String.valueOf(rawItem));

            if (StringUtils.hasText(item)) {
                items.add(item);
            }
        }

        return items;
    }

    private Map<String, Object> firstNonEmptyMap(Map<String, Object> first, Map<String, Object> second) {
        return first.isEmpty() ? second : first;
    }

    private String fallbackText(String... values) {
        for (String value : values) {
            String normalizedValue = trimToEmpty(value);

            if (StringUtils.hasText(normalizedValue)) {
                return normalizedValue;
            }
        }

        return "";
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        String normalizedValue = trimToEmpty(value);
        return StringUtils.hasText(normalizedValue) ? normalizedValue : null;
    }

    private String extractLastSegment(String value) {
        return searchQuerySupport.extractLastSegment(value);
    }

    private String normalizeIdentifierLabel(String value) {
        return trimToEmpty(value)
                .replaceFirst("(?i)^https?://doi\\.org/", "")
                .replaceFirst("(?i)^https?://pubmed\\.ncbi\\.nlm\\.nih\\.gov/", "")
                .replaceFirst("(?i)^https?://openalex\\.org/", "");
    }

    private String normalizePubmedUrl(String value) {
        String normalizedValue = trimToEmpty(value);

        if (!StringUtils.hasText(normalizedValue)) {
            return "";
        }

        if (normalizedValue.matches("(?i)^https?://.*")) {
            return normalizedValue;
        }

        String pubmedId = normalizedValue.replaceFirst("(?i)^pmid:", "").trim();
        return StringUtils.hasText(pubmedId)
                ? "https://pubmed.ncbi.nlm.nih.gov/" + pubmedId
                : "";
    }

    private String normalizeOpenAlexWorkId(String value) {
        return extractLastSegment(value).toUpperCase(Locale.ROOT);
    }

    private String normalizeWorkLinkLabel(String label) {
        return trimToEmpty(label)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String formatTypeLabel(String value) {
        String normalizedValue = trimToEmpty(value);

        if (!StringUtils.hasText(normalizedValue)) {
            return "";
        }

        String[] words = normalizedValue.replace('-', ' ').replace('_', ' ').split("\\s+");
        List<String> formattedWords = new ArrayList<>();

        for (String word : words) {
            if (!StringUtils.hasText(word)) {
                continue;
            }

            formattedWords.add(word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1));
        }

        return String.join(" ", formattedWords);
    }

    private String formatLanguageLabel(String languageCode) {
        String normalizedLanguageCode = trimToEmpty(languageCode);

        if (!StringUtils.hasText(normalizedLanguageCode)) {
            return "";
        }

        String displayLanguage = Locale.forLanguageTag(normalizedLanguageCode).getDisplayLanguage(Locale.ENGLISH);
        return StringUtils.hasText(displayLanguage) ? displayLanguage : normalizedLanguageCode.toUpperCase(Locale.ROOT);
    }

    private String formatCountryLabel(String countryCode) {
        String normalizedCountryCode = trimToEmpty(countryCode).toUpperCase(Locale.ROOT);

        if (!StringUtils.hasText(normalizedCountryCode)) {
            return "";
        }

        String displayCountry = new Locale("", normalizedCountryCode).getDisplayCountry(Locale.ENGLISH);
        return StringUtils.hasText(displayCountry) ? displayCountry : normalizedCountryCode;
    }

    private String formatPublishedLabel(Integer publicationYear, String publicationDate) {
        if (StringUtils.hasText(publicationDate)) {
            try {
                LocalDate date = LocalDate.parse(publicationDate.trim());
                return "Published " + PUBLISHED_DATE_FORMATTER.format(date);
            } catch (RuntimeException ignored) {
            }
        }

        if (publicationYear != null) {
            return "Published " + publicationYear;
        }

        return "";
    }

    private String formatOpenAccessStatus(String status) {
        String normalizedStatus = trimToEmpty(status);
        return StringUtils.hasText(normalizedStatus) ? normalizedStatus.toUpperCase(Locale.ROOT) + " OA" : "";
    }

    private String formatLicenseLabel(String license) {
        String normalizedLicense = trimToEmpty(license);
        return StringUtils.hasText(normalizedLicense) ? normalizedLicense.toUpperCase(Locale.ROOT) : "";
    }

    private String formatAvailabilityLabel(boolean isAvailable) {
        return isAvailable ? "Available" : "Not available";
    }

    private String formatCurrency(Integer value, String currency) {
        if (value == null) {
            return "";
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        formatter.setMaximumFractionDigits(0);

        try {
            formatter.setCurrency(Currency.getInstance(fallbackText(currency, "USD")));
        } catch (IllegalArgumentException ignored) {
            formatter.setCurrency(Currency.getInstance("USD"));
        }

        return formatter.format(value);
    }

    private String formatFullNumber(long value) {
        return NumberFormat.getIntegerInstance(Locale.US).format(value);
    }

    private String formatDecimalValue(double value) {
        if (Double.isNaN(value)) {
            return "";
        }

        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(value);
    }

    private String formatCitationPercentile(double value) {
        if (Double.isNaN(value)) {
            return "";
        }

        NumberFormat formatter = NumberFormat.getPercentInstance(Locale.US);
        formatter.setMaximumFractionDigits(1);
        return formatter.format(value);
    }

    private String formatHostnameLabel(String value) {
        try {
            String host = URI.create(value).getHost();
            return StringUtils.hasText(host)
                    ? host.replaceFirst("(?i)^www\\.", "")
                    : value;
        } catch (IllegalArgumentException exception) {
            return value;
        }
    }
}
