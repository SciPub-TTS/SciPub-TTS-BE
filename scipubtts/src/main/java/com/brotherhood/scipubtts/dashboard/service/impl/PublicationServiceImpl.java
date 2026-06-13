package com.brotherhood.scipubtts.dashboard.service.impl;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.dashboard.dto.request.PublicationTrendRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexPublicationRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.PublicationTrendCalculateResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.PublicationTrendResponse;
import com.brotherhood.scipubtts.dashboard.entity.PublicationTrend;
import com.brotherhood.scipubtts.dashboard.repository.PublicationTrendRepository;
import com.brotherhood.scipubtts.dashboard.service.PublicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PublicationServiceImpl implements PublicationService {
  private final OpenAlexServiceImpl openAlexService;
  private final PublicationTrendRepository publicationTrendRepository;

  private PublicationTrendCalculateResponse takePublicationTrends(OpenAlexPublicationRequest request){

    var data = openAlexService.searchPublicationsByYear(request);

    if (data == null || data.groupBy() == null || data.groupBy().isEmpty()) {
      throw new BusinessException(
              ErrorCode.OPENALEX_SERVICE_ERROR
      );
    }

    var publicationTrends =
            data.groupBy()
                    .stream()
                    .map(item ->
                            new PublicationTrend(
                                    Integer.parseInt(
                                            item.key()
                                    ),
                                    item.count()
                            )
                    )
                    .toList();

    return new PublicationTrendCalculateResponse(
            publicationTrends
    );
  }

  public PublicationTrendCalculateResponse calculateAndSavePublicationTrends(
          OpenAlexPublicationRequest request
  ) {

    var response = takePublicationTrends(request);

    for (var item : response.publicationTrends()) {

      var entity = publicationTrendRepository
              .findById(item.getYear())
              .orElseGet(() ->
                      PublicationTrend.builder()
                              .year(item.getYear())
                              .build()
              );

      entity.setPublications(
              item.getPublications()
      );

      publicationTrendRepository.save(entity);
    }

    return response;
  }

  public PublicationTrendResponse getPublicationTrendsFromDb() {

    List<PublicationTrendResponse.PublicationTrendItem> result =
            publicationTrendRepository.findAll()
                    .stream()
                    .sorted(
                            Comparator.comparing(
                                    PublicationTrend::getYear
                            )
                    )
                    .map(item ->
                            new PublicationTrendResponse.PublicationTrendItem(
                                    item.getPublications(),
                                    item.getYear()
                            )
                    )
                    .toList();

    return new PublicationTrendResponse(result);
  }

  public PublicationTrendResponse getPublicationTrendsFromDb(
          PublicationTrendRequest request
  ) {

    List<PublicationTrendResponse.PublicationTrendItem> result =
            publicationTrendRepository
                    .findByYearBetweenOrderByYear(
                            request.startYear(),
                            request.endYear()
                    )
                    .stream()
                    .map(item ->
                            new PublicationTrendResponse.PublicationTrendItem(
                                    item.getPublications(),
                                    item.getYear()
                            )
                    )
                    .toList();

    return new PublicationTrendResponse(result);
  }
}