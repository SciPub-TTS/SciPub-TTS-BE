package com.brotherhood.scipubtts.feed.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAlexWorksResponse(
        List<OpenAlexWork> results,
        Meta meta
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OpenAlexWork(
            String id,

            @JsonProperty("display_name")
            String displayName,

            @JsonProperty("publication_year")
            Integer publicationYear,

            @JsonProperty("publication_date")
            LocalDate publicationDate,

            @JsonProperty("cited_by_count")
            Integer citedByCount,

            List<Authorship> authorships,

            @JsonProperty("primary_location")
            PrimaryLocation primaryLocation,

            String doi,

            String type,

            List<Topic> topics,

            List<Keyword> keywords,

            @JsonProperty("abstract_inverted_index")
            Map<String, List<Integer>> abstractInvertedIndex
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Authorship(
            Author author,
            List<Institution> institutions
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Author(
            String id,

            @JsonProperty("display_name")
            String displayName
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Institution(
            String id,

            @JsonProperty("display_name")
            String displayName,

            @JsonProperty("country_code")
            String countryCode
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PrimaryLocation(
            Source source,

            @JsonProperty("pdf_url")
            String pdfUrl
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Source(
            String id,

            @JsonProperty("display_name")
            String displayName
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Topic(
            String id,

            @JsonProperty("display_name")
            String displayName,

            double score,

            Field field,

            Subfield subfield
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Field(
            String id,

            @JsonProperty("display_name")
            String displayName
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Subfield(
            String id,

            @JsonProperty("display_name")
            String displayName
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Keyword(
            String id,

            @JsonProperty("display_name")
            String displayName,

            double score
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(
            @JsonProperty("next_cursor")
            String nextCursor
    ) {}
}
