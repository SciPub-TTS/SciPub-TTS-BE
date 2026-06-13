package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexPublicationRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.PublicationTrendCalculateResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.PublicationTrendResponse;

public interface PublicationService {
  PublicationTrendCalculateResponse calculateAndSavePublicationTrends(
          OpenAlexPublicationRequest request
  );

  PublicationTrendResponse getPublicationTrendsFromDb();
}