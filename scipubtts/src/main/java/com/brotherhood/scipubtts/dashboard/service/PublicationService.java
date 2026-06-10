package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexPublicationRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.PublicationTrendResponse;

public interface PublicationService {
  PublicationTrendResponse calculateAndSavePublicationTrends(
          OpenAlexPublicationRequest request
  );

  PublicationTrendResponse getPublicationTrendsFromDb();
}