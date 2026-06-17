package com.brotherhood.scipubtts.feed.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record OpenAlexWorksResponse(
        OpenAlexMeta meta,
        List<OpenAlexWork> results
) {
    public record OpenAlexMeta(
            Integer count,

            @JsonProperty("per_page")
            Integer perPage,

            @JsonProperty("next_cursor")
            String nextCursor
    ) {
    }

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
            PrimaryLocation primaryLocation
    ) {
    }

    public record Authorship(
            Author author
    ) {
    }

    public record Author(
            String id,

            @JsonProperty("display_name")
            String displayName
    ) {
    }

    public record PrimaryLocation(
            Source source
    ) {
    }

    public record Source(
            String id,

            @JsonProperty("display_name")
            String displayName
    ) {
    }
}