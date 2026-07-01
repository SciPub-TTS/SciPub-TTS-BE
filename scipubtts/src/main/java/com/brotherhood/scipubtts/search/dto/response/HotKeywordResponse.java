package com.brotherhood.scipubtts.search.dto.response;

import java.time.LocalDate;
import java.util.List;

public record HotKeywordResponse(
        LocalDate snapshotDate,
        List<HotKeywordItemResponse> keywords
) {
}
