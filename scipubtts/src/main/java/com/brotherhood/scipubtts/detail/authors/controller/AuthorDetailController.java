package com.brotherhood.scipubtts.detail.authors.controller;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.detail.authors.dto.response.AuthorDetailResponse;
import com.brotherhood.scipubtts.detail.authors.service.AuthorDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authors")
public class AuthorDetailController {

    private final AuthorDetailService authorDetailService;

    public AuthorDetailController(AuthorDetailService authorDetailService) {
        this.authorDetailService = authorDetailService;
    }

    @GetMapping("/{authorId}")
    @Operation(summary = "Get author detail")
    public ResponseEntity<ResponseObject> getAuthorDetail(
            @Parameter(example = "A1969205032")
            @PathVariable String authorId
    ) {
        AuthorDetailResponse data = authorDetailService.getAuthorDetail(authorId);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded author detail", data)
        );
    }
}
