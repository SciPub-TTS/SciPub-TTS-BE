package com.brotherhood.scipubtts.report.dto.response;

public record ExportReportResult(
        byte[] content,
        String fileName,
        String contentType
) {}
