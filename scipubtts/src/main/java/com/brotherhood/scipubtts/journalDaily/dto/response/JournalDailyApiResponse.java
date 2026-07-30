package com.brotherhood.scipubtts.journalDaily.dto.response;

import com.brotherhood.scipubtts.journalDaily.dto.request.JournalDailyResultItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JournalDailyApiResponse(
        @JsonProperty("response")
        JournalDailyApiResponse.ResponseWrapper response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResponseWrapper(
            String status,
            List<JournalDailyResultItem> results
    ) {}
}
