package com.brotherhood.scipubtts.canvas.controller;

import com.brotherhood.scipubtts.canvas.service.CanvasService;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/canvas")
public class CanvasController {

    private final CanvasService canvasService;

    public CanvasController(CanvasService canvasService) {
        this.canvasService = canvasService;
    }

    @GetMapping("/entities/{entityType}/{entityId}")
    public ResponseEntity<ResponseObject> getEntityDetail(
            @PathVariable String entityType,
            @PathVariable String entityId
    ) {
        Map<String, Object> data = canvasService.getEntityDetail(entityType, entityId);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Loaded canvas entity detail", data)
        );
    }

    @GetMapping("/entities/{entityType}/{entityId}/works")
    public ResponseEntity<ResponseObject> getEntityTopWorks(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @RequestParam(required = false) Integer page,
            @RequestParam(name = "per_page", required = false) Integer perPage,
            @RequestParam(required = false) String sort
    ) {
        Map<String, Object> data = canvasService.getEntityTopWorks(
                entityType,
                entityId,
                page,
                perPage,
                sort
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Loaded canvas top works", data)
        );
    }
}
