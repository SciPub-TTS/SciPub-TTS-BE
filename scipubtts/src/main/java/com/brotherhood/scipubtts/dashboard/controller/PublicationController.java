package com.brotherhood.scipubtts.dashboard.controller;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.dashboard.dto.request.OpenAlexPublicationRequest;
import com.brotherhood.scipubtts.dashboard.service.PublicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistic")
public class PublicationController {
  private final PublicationService publicationService;

  public PublicationController(PublicationService publicationService) {
    this.publicationService = publicationService;
  }

  @PostMapping("/publication-trends")
  public ResponseEntity<ResponseObject> takePublicationTrend(@Valid @RequestBody OpenAlexPublicationRequest request, HttpServletRequest httpServletRequest){
    var data = publicationService.takePublicationTrends(request);

    return ResponseEntity.ok(
            new ResponseObject(
                    HttpStatus.OK.value(),
                    "Get publication trends successfully",
                    data
            )
    );
  }
}