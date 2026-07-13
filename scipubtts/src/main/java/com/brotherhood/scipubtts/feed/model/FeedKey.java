package com.brotherhood.scipubtts.feed.model;

import java.util.UUID;

public record FeedKey(
        UUID userId,
        String workOpenalexId
) {
}