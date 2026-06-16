package com.brotherhood.scipubtts.feed.model;

public record FeedReason(
        String type,
        String targetOpenalexId,
        String displayName
) {
}