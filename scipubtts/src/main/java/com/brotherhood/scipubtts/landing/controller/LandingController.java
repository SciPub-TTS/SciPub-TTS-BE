package com.brotherhood.scipubtts.landing.controller;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.landing.dto.response.LandingTrendPreviewResponse;
import com.brotherhood.scipubtts.landing.service.LandingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("api/home/landing")
@RequiredArgsConstructor
public class LandingController {

    private final LandingService landingService;

    @GetMapping("/preview")
    public ResponseEntity<ResponseObject> getLandingTrendPreview(
            @RequestParam(required = false) LocalDate snapshotDate
    ) {
        LandingTrendPreviewResponse data = landingService.getTrendPreview(snapshotDate);

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Loaded landing trend preview",
                        data
                )
        );
    }
}
