package com.brotherhood.scipubtts.detail.works.dto.response;

import java.util.List;

public record PaperDetailResponse(
        String abstractText,
        List<SummaryItem> accessItems,
        List<AuthorItem> authors,
        List<String> awards,
        long citationCount,
        String doiHref,
        String doiLabel,
        List<BadgeItem> headerBadges,
        List<String> indexedIn,
        List<InstitutionItem> institutions,
        List<CountryItem> countries,
        List<MetricItem> items,
        List<String> keywords,
        String languageLabel,
        String openAlexId,
        String pdfUrl,
        Integer publicationYear,
        String publishedLabel,
        List<QuickLinkItem> quickLinks,
        List<WorkLinkItem> referencedWorks,
        List<WorkLinkItem> relatedWorks,
        EntityRef source,
        String sourceHostOrganization,
        String sourceName,
        String sourceType,
        String title,
        List<EntityRef> topics,
        String workType
) {
    public record SummaryItem(
            String label,
            String value,
            String href
    ) {
    }

    public record MetricItem(
            String label,
            String value
    ) {
    }

    public record EntityRef(
            String id,
            String name,
            String type
    ) {
    }

    public record WorkLinkItem(
            String id,
            String label
    ) {
    }

    public record BadgeItem(
            String label,
            String tone,
            String entityId,
            String entityType
    ) {
    }

    public record QuickLinkItem(
            String href,
            String label,
            String value
    ) {
    }

    public record AuthorItem(
            String id,
            String entityId,
            boolean isCorresponding,
            boolean isFollowed,
            String name,
            String orcid,
            String position
    ) {
    }

    public record InstitutionItem(
            String countryName,
            String countryCode,
            String id,
            String name,
            String type
    ) {
    }

    public record CountryItem(
            String code,
            String name
    ) {
    }
}
