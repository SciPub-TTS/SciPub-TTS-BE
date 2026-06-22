package com.brotherhood.scipubtts.report.service;

import com.brotherhood.scipubtts.report.dto.request.ExportReportRequest;
import com.brotherhood.scipubtts.report.dto.response.ExportReportResult;

public interface ExportReportService {

    /**
     * Tạo file report (CSV hoặc JSON) từ danh sách paperIds + fields đã chọn.
     *
     * Business rules áp dụng:
     * - Tối đa 20 paperIds (validate ở DTO, double-check ở Service).
     * - Ít nhất 1 field (validate ở DTO).
     * - Field đa giá trị (Authors, Institutions, Countries, Topics, Keywords)
     *   gộp thành 1 chuỗi, phân tách bằng " | ".
     * - Field thiếu dữ liệu → hiển thị "N/A".
     * - CSV escape bằng CSV library (Apache Commons CSV), không tự nối chuỗi.
     * - Tên file: papers_report_YYYYMMDD.csv|json
     */
    ExportReportResult exportReport(ExportReportRequest request);
}
