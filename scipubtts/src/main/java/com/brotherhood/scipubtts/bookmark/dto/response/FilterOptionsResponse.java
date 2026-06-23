package com.brotherhood.scipubtts.bookmark.dto.response;

import java.util.List;

public record FilterOptionsResponse(
        List<String> topics,
        List<Integer> years,
        List<String> authors
) {
}
