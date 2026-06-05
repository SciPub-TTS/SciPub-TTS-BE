package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.constant.OpenAlexEntity;
import com.brotherhood.scipubtts.dashboard.dto.request.OpenAlexMetricsInPeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.OpenAlexMetricsToPeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.OpenAlexPublicationRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.OpenAlexMetricsResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.OpenAlexPublicationResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenAlexService {
  private final RestClient restClient;

  public OpenAlexService(RestClient openAlexClient) {
    this.restClient = openAlexClient;
  }

  public OpenAlexPublicationResponse searchPublicationsByYear(OpenAlexPublicationRequest request){
    if(request.yearFrom().isEmpty() || request.yearTo().isEmpty()) {
      return null;
    }

    var queryPeriod = String.format("publication_year:%s-%s", request.yearFrom(), request.yearTo());

    return restClient.get()
            .uri(uriBuilder ->
                    uriBuilder
                            .path("/works")
                            .queryParam(
                                    "filter",
                                    queryPeriod
                            )
                            .queryParam(
                                    "group_by",
                                    "publication_year"
                            )
                            .build())
            .retrieve()
            .body(OpenAlexPublicationResponse.class);
  }

  public OpenAlexMetricsResponse takeMetricsInPeriod(OpenAlexMetricsInPeriodRequest request){
    if(request.endTime().isEmpty() || request.startTime().isEmpty()){
      return null;
    }

    var queryPeriod = String.format("from_publication_date:%s,to_publication_date:%s",
            request.startTime(), request.endTime());

    return restClient.get()
            .uri(uriBuilder ->
                    uriBuilder
                            .path("/works")
                            .queryParam(
                                    "filter",
                                    queryPeriod
                            )
                            .queryParam(
                                    "page",
                                    1
                            ).queryParam(
                                    "per_page",
                                    1
                            ).queryParam(
                                    "select",
                                  "id"
                            )
                            .build())
            .retrieve()
            .body(OpenAlexMetricsResponse.class);
  }

  public OpenAlexMetricsResponse takeMetricsToPeriod(
          OpenAlexMetricsToPeriodRequest request) {
    var path = String.format(("/%s"), request.entity().getPath());

    return restClient.get()
            .uri(uriBuilder ->
                    uriBuilder
                            .path(path)
                            .queryParam(
                                    "page",
                                    1
                            ).queryParam(
                                    "per_page",
                                    1
                            ).queryParam(
                                    "select",
                                    "id"
                            )
                            .build())
            .retrieve()
            .body(OpenAlexMetricsResponse.class);
  }
}