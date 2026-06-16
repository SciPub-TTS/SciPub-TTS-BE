package com.brotherhood.scipubtts.follow.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FollowPageResponse {
    private List<FollowResponse> items;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    private boolean hasNext;

    public List<FollowResponse> items() {
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

