package com.brotherhood.scipubtts.detail.entities.controller;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.detail.entities.dto.response.EntityDetailResponse;
import com.brotherhood.scipubtts.detail.entities.service.EntityDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EntityDetailController {

    private final EntityDetailService entityDetailService;

    public EntityDetailController(EntityDetailService entityDetailService) {
        this.entityDetailService = entityDetailService;
    }

    @GetMapping("/authors/{authorId}")
    @Operation(summary = "Get author detail")
    public ResponseEntity<ResponseObject> getAuthorDetail(
            @Parameter(example = "A5053271482")
            @PathVariable String authorId
    ) {
        EntityDetailResponse data = entityDetailService.getAuthorDetail(authorId);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(HttpStatus.OK.value(), "Loaded author detail", data)
        );
    }

    @GetMapping("/topics/{topicId}")
    @Operation(summary = "Get topic detail")
    public ResponseEntity<ResponseObject> getTopicDetail(
            @Parameter(example = "T14423")
            @PathVariable String topicId
    ) {
        EntityDetailResponse data = entityDetailService.getTopicDetail(topicId);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(HttpStatus.OK.value(), "Loaded topic detail", data)
        );
    }
}
