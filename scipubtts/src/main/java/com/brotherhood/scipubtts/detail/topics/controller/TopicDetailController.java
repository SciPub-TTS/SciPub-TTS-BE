package com.brotherhood.scipubtts.detail.topics.controller;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.detail.topics.dto.response.TopicDetailResponse;
import com.brotherhood.scipubtts.detail.topics.service.TopicDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/topics")
public class TopicDetailController {

    private final TopicDetailService topicDetailService;

    public TopicDetailController(TopicDetailService topicDetailService) {
        this.topicDetailService = topicDetailService;
    }

    @GetMapping("/{topicId}")
    @Operation(summary = "Get topic detail")
    public ResponseEntity<ResponseObject> getTopicDetail(
            @Parameter(example = "T10177")
            @PathVariable String topicId
    ) {
        TopicDetailResponse data = topicDetailService.getTopicDetail(topicId);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded topic detail", data)
        );
    }
}
