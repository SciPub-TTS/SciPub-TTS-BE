package com.brotherhood.scipubtts.feed.model;

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
    private OffsetDateTime generatedAt;

    private String authorOpenAlexIdsSnapshot;
    private String workTypeSnapshot;
    private String topicSnapshot;
    private String topicOpenAlexIdSnapshot;
    private String abstractText;
    private String doi;
    private String pdfUrl;
    private String keywordsJson;
    private String primaryFieldSnapshot;
    private String subfieldSnapshot;

    private final Set<FeedReason> reasons = new LinkedHashSet<>();
}
