package com.brotherhood.scipubtts.bookmark.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkResponse {
    private UUID id;

    private String openAlexId;

    private String title;

    private String authors;

    private String source;

    private String topic;

    private Integer publicationYear;

    private Integer citationCount;

    private String note;

    private OffsetDateTime createdAt;

    public UUID id() {
        return id;
    }

    public String openAlexId() {
        return openAlexId;
    }

    public String title() {
        return title;
    }

    public String authors() {
        return authors;
    }

    public String source() {
        return source;
    }

    public String topic() {
        return topic;
    }

    public Integer publicationYear() {
        return publicationYear;
    }

    public Integer citationCount() {
        return citationCount;
    }

    public String note() {
        return note;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }
}

