package com.brotherhood.scipubtts.bookmark.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@SuppressWarnings("unused")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkPageResponse {
    private List<BookmarkResponse> items;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    private boolean hasNext;

    public List<BookmarkResponse> items() {
        return items;
    }

    public int page() {
        return page;
    }

    public int size() {
        return size;
    }

    public long totalElements() {
        return totalElements;
    }

    public int totalPages() {
        return totalPages;
    }

    public boolean hasNext() {
        return hasNext;
    }
}

