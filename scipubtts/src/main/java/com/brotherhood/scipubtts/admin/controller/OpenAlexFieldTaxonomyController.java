package com.brotherhood.scipubtts.admin.controller;

import com.brotherhood.scipubtts.admin.service.OpenAlexFieldTaxonomySyncService;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/openalex/field-taxonomy")
@RequiredArgsConstructor
public class OpenAlexFieldTaxonomyController {

    private final OpenAlexFieldTaxonomySyncService openAlexFieldTaxonomySyncService;

    @PostMapping("/sync")
    public ResponseEntity<ResponseObject> syncFieldTaxonomy() {
        openAlexFieldTaxonomySyncService.syncFieldTaxonomy();

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "OpenAlex field taxonomy synced successfully",
                        null
                )
        );
    }
}
