package com.brotherhood.scipubtts.landing.service;

import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.landing.dto.response.OpenAlexStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final OpenAlexClient openAlexClient;

    public OpenAlexStatisticsResponse getStatistics() {

        return OpenAlexStatisticsResponse.builder()
                .totalPapers(openAlexClient.getWorksCount())
                .totalAuthors(openAlexClient.getAuthorsCount())
                .totalTopics(openAlexClient.getTopicsCount())
                .totalFields(openAlexClient.getFieldsCount())
                .build();
    }
}
