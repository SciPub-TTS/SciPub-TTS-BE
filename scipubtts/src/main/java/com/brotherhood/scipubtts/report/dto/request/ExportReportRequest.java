package com.brotherhood.scipubtts.report.dto.request;

import com.brotherhood.scipubtts.report.enums.ExportField;
import com.brotherhood.scipubtts.report.enums.ExportFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ExportReportRequest(
        @NotEmpty(message = "At least one paper must be selected for export")
        @Size(max = 20, message = "A maximum of 20 papers can be exported at a time")
        List<String> paperIds,

        @NotEmpty(message = "At least one data field must be selected for export")
        List<ExportField> fields,

        @NotNull(message = "Export format is required (CSV or JSON)")
        ExportFormat format,

        boolean includeMetadata,

        // Optional — chỉ dùng để ghi vào metadata "searchQuery", không ảnh hưởng logic fetch dữ liệu
        String searchQuery

) {
}
