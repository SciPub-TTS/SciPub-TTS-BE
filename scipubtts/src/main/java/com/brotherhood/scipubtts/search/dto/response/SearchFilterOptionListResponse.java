package com.brotherhood.scipubtts.search.dto.response;

import java.util.List;

public record SearchFilterOptionListResponse(
        String filterKey,
        List<OptionItem> options
) {
    public record OptionItem(
            String value,
            String label,
            long count
    ) {
    }
}
