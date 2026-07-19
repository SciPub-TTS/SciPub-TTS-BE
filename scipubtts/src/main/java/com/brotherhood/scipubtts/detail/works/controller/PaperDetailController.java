package com.brotherhood.scipubtts.detail.works.controller;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.detail.works.dto.response.PaperDetailResponse;
import com.brotherhood.scipubtts.detail.works.service.PaperDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/papers")
public class PaperDetailController {

    private final PaperDetailService paperDetailService;

    public PaperDetailController(PaperDetailService paperDetailService) {
        this.paperDetailService = paperDetailService;
    }

    @GetMapping("/{workId}")
    @Operation(summary = "Get work detail")
    public ResponseEntity<ResponseObject> getWorkDetail(
            @Parameter(example = "W2125121305")
            @PathVariable String workId
    ) {
        PaperDetailResponse data = paperDetailService.getWorkDetail(workId);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded work detail", data)
        );
    }
}
