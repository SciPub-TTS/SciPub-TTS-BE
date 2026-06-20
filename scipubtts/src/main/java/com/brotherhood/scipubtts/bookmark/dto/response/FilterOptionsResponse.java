package com.brotherhood.scipubtts.bookmark.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FilterOptionsResponse {
    private List<String> topics;

    private List<Integer> years;

    private List<String> authors;

    public List<String> topics() {
        return topics;
    }

    public List<Integer> years() {
        return years;
    }

    public List<String> authors() {
        return authors;
    }
}

