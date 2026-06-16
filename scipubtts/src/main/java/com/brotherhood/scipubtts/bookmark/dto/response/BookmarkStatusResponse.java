package com.brotherhood.scipubtts.bookmark.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkStatusResponse {
    private boolean bookmarked;

    private UUID bookmarkId;

    private String openAlexid;

    public boolean bookmarked() {
        return bookmarked;
    }

    public UUID bookmarkId() {
        return bookmarkId;
    }

    public String openAlexid() {
        return openAlexid;
    }
}

