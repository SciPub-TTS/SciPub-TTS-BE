package com.brotherhood.scipubtts.openalexentity.controller;

import com.brotherhood.scipubtts.openalexentity.service.OpenAlexEntityService;
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
@RequestMapping("/api/openalex-entities")
public class OpenAlexEntityController {

    private final OpenAlexEntityService openAlexEntityService;

    public OpenAlexEntityController(OpenAlexEntityService openAlexEntityService) {
        this.openAlexEntityService = openAlexEntityService;
    }

    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<ResponseObject> getEntityDetail(
            @PathVariable String entityType,
            @PathVariable String entityId
    ) {
        Map<String, Object> data = openAlexEntityService.getEntityDetail(entityType, entityId);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Loaded OpenAlex entity detail", data)
        );
    }

    @GetMapping("/{entityType}/{entityId}/works")
    public ResponseEntity<ResponseObject> getEntityTopWorks(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @RequestParam(required = false) Integer page,
            @RequestParam(name = "per_page", required = false) Integer perPage,
            @RequestParam(required = false) String sort
    ) {
        Map<String, Object> data = openAlexEntityService.getEntityTopWorks(
                entityType,
                entityId,
                page,
                perPage,
                sort
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Loaded OpenAlex entity top works", data)
        );
    }
}
