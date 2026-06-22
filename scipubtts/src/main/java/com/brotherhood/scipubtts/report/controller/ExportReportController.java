package com.brotherhood.scipubtts.report.controller;

import com.brotherhood.scipubtts.report.dto.request.ExportReportRequest;
import com.brotherhood.scipubtts.report.dto.response.ExportReportResult;
import com.brotherhood.scipubtts.report.service.ExportReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportReportController {

    private final ExportReportService exportReportService;

    /**
     * POST /api/export/report
     * Body: ExportReportRequest (paperIds, fields, format, includeMetadata, searchQuery)
     * Trả về file CSV/JSON dưới dạng attachment để FE trigger download trực tiếp.
     */
    @PostMapping("/report")
    public ResponseEntity<byte[]> exportReport(@Valid @RequestBody ExportReportRequest request) {

        ExportReportResult result = exportReportService.exportReport(request);

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(result.fileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.content());
    }
}
