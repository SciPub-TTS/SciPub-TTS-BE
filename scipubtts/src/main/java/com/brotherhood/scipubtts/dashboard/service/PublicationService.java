package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.dashboard.dto.request.OpenAlexPublicationRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.PublicationTrendResponse;
import com.brotherhood.scipubtts.dashboard.entity.PublicationTrend;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PublicationService {
  private final OpenAlexService openAlexService;

  public PublicationTrendResponse takePublicationTrends(OpenAlexPublicationRequest request){

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

    return new PublicationTrendResponse(
            publicationTrends
    );
  }
}