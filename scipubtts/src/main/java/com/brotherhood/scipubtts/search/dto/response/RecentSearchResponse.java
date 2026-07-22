package com.brotherhood.scipubtts.search.dto.response;

import java.time.OffsetDateTime;

public record RecentSearchResponse(
        String content,
        OffsetDateTime latestCreatedAt
) {}