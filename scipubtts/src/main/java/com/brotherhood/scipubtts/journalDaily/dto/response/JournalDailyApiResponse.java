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
            int total,          // Tổng số bài báo tìm thấy
            int startIndex,     // Vị trí bắt đầu
            int pageSize,       // Kích thước 1 trang
            int currentPage,    // Trang hiện tại
            int pages,          // Tổng số trang (CẦN CHO VÒNG LẶP)
            String orderBy,
            List<JournalDailyResultItem> results
    ) {}
}