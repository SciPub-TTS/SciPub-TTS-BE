package com.brotherhood.scipubtts.dashboard.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.brotherhood.scipubtts.dashboard.entity.Keyword;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KeywordHotFilterResponse {
    private List<Keyword> keywordList;

    public List<Keyword> keywordList() {
        return keywordList;
    }
}
