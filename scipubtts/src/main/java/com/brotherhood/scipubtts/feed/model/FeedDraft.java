package com.brotherhood.scipubtts.feed.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Data
public class FeedDraft {

    private final UUID userId;
    private final String workOpenalexId;

    private String titleSnapshot;
    private String authorsSnapshot;
    private String sourceSnapshot;
    private Integer publicationYear;
    private LocalDate publicationDate;
    private Integer citationSnapshot;
    private double relevanceScore;
    private OffsetDateTime generatedAt;

    private final Set<FeedReason> reasons = new LinkedHashSet<>();
}
