package com.brotherhood.scipubtts.journalDaily.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JournalDailyResultItem(

        // Guardian's own article id — dùng làm external_id
        String id,
        String type,
        String webTitle,
        String webUrl,
        String webPublicationDate,
        String sectionName,

        // fields object — cần show-fields=trailText,thumbnail,byline trong request
        JournalDailyResultItem.GuardianFields fields,

        // tags array — cần show-tags=keyword trong request
        List<JournalDailyResultItem.GuardianTag> tags
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GuardianFields(
            @JsonProperty("trailText")
            String trailText,

            @JsonProperty("byline")
            String byline,

            @JsonProperty("thumbnail")
            String thumbnail
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GuardianTag(
            String webTitle
    ) {
    }
}
