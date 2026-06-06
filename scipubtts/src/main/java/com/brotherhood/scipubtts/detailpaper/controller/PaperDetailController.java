package com.brotherhood.scipubtts.detailpaper.controller;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.detailpaper.service.PaperDetailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/papers")
public class PaperDetailController {

    private final PaperDetailService paperDetailService;

    public PaperDetailController(PaperDetailService paperDetailService) {
        this.paperDetailService = paperDetailService;
    }

    @GetMapping("/{workId}")
    public ResponseEntity<ResponseObject> getWorkDetail(@PathVariable String workId) {
        Map<String, Object> data = paperDetailService.getWorkDetail(workId);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Loaded work detail", data)
        );
    }
}
