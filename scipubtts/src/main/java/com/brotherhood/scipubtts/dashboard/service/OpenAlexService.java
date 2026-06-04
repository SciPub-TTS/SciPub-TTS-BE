package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.dto.request.OpenAlexPublicationRequest;
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
}