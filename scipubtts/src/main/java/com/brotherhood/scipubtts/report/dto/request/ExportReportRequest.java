package com.brotherhood.scipubtts.report.dto.request;

import com.brotherhood.scipubtts.report.enums.ExportField;
import com.brotherhood.scipubtts.report.enums.ExportFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ExportReportRequest(
        @NotEmpty(message = "Phải chọn ít nhất 1 bài báo để export")
        @Size(max = 20, message = "Chỉ được export tối đa 20 bài báo")
        List<String> paperIds,

        @NotEmpty(message = "Phải chọn ít nhất 1 trường dữ liệu để export")
        List<ExportField> fields,

        @NotNull(message = "Phải chọn định dạng export (CSV hoặc JSON)")
        ExportFormat format,

        boolean includeMetadata,

        // Optional — chỉ dùng để ghi vào metadata "searchQuery", không ảnh hưởng logic fetch dữ liệu
        String searchQuery

) {
}
