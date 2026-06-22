package com.brotherhood.scipubtts.bookmark.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkStatsResponse {
    private int totalPapers;

    private int totalTopics;


    private int totalAuthors;

    public int totalPapers() {
        return totalPapers;
    }

    public int totalTopics() {
        return totalTopics;
    }

    public int totalAuthors() {
        return totalAuthors;
    }
}

