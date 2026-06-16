package com.brotherhood.scipubtts.dashboard.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.brotherhood.scipubtts.dashboard.entity.PublicationTrend;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublicationTrendCalculateResponse {
    private List<PublicationTrend> publicationTrends;

    public List<PublicationTrend> publicationTrends() {
        return publicationTrends;
    }
}
