package com.brotherhood.scipubtts.report.service.impl;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.report.dto.response.PaperExportData;
import com.brotherhood.scipubtts.report.dto.request.ExportReportRequest;
import com.brotherhood.scipubtts.report.dto.response.ExportMetadata;
import com.brotherhood.scipubtts.report.dto.response.ExportReportResult;
import com.brotherhood.scipubtts.report.enums.ExportField;
import com.brotherhood.scipubtts.report.enums.ExportFormat;
import com.brotherhood.scipubtts.report.service.ExportReportService;
import com.brotherhood.scipubtts.report.service.PaperExportDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportReportServiceImpl implements ExportReportService {

    private static final int MAX_PAPERS = 20;
    private static final String FIELD_SEPARATOR = " | ";
    private static final String NOT_AVAILABLE = "N/A";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PaperExportDataService paperExportDataService;
    private final ObjectMapper objectMapper;

    @Override
    public ExportReportResult exportReport(ExportReportRequest request) {

        // ── Validate business rules (double-check, DTO @Valid đã chặn phần lớn) ──
        if (request.paperIds() == null || request.paperIds().isEmpty()) {
            throw new BusinessException(ErrorCode.EXPORT_NO_PAPER_SELECTED);
        }
        if (request.paperIds().size() > MAX_PAPERS) {
            throw new BusinessException(ErrorCode.EXPORT_EXCEEDS_PAPER_LIMIT);
        }
        if (request.fields() == null || request.fields().isEmpty()) {
            throw new BusinessException(ErrorCode.EXPORT_NO_FIELD_SELECTED);
        }
        if (request.format() == null) {
            throw new BusinessException(ErrorCode.EXPORT_FORMAT_REQUIRED);
        }

        // ── Lấy dữ liệu paper (giữ thứ tự người dùng chọn) ──
        List<PaperExportData> papers = paperExportDataService.fetchPapersForExport(request.paperIds());

        if (papers.isEmpty()) {
            throw new BusinessException(ErrorCode.EXPORT_NO_PAPER_FOUND);
        }

        // ── Build metadata nếu cần ──
        ExportMetadata metadata = request.includeMetadata() ? buildMetadata(request, papers.size()) : null;

        // ── Render theo format ──
        byte[] content = switch (request.format()) {
            case CSV -> renderCsv(papers, request.fields(), metadata);
            case JSON -> renderJson(papers, request.fields(), metadata);
        };

        String fileName = buildFileName(request.format());
        String contentType = request.format() == ExportFormat.CSV
                ? "text/csv; charset=UTF-8"
                : "application/json; charset=UTF-8";

        return new ExportReportResult(content, fileName, contentType);
    }

    // ─────────────────────────────────────────────────────────
    // CSV RENDERING — dùng Apache Commons CSV để escape chuẩn
    // ─────────────────────────────────────────────────────────

    private byte[] renderCsv(List<PaperExportData> papers, List<ExportField> fields, ExportMetadata metadata) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {

            // Metadata được ghi dưới dạng các dòng comment phía trên bảng dữ liệu,
            // để không phá vỡ cấu trúc cột chính của CSV.
            if (metadata != null) {
                writer.write("# Export Date: " + metadata.exportDate() + "\n");
                writer.write("# Search Query: " + escapeForCommentLine(metadata.searchQuery()) + "\n");
                writer.write("# Total Selected Papers: " + metadata.selectedPaperCount() + "\n");
                String fieldsString = String.join(", ", metadata.selectedFields());
                writer.write("\"# Selected Fields: " + fieldsString + "\"\n");
                writer.write("# Format: " + metadata.format() + "\n");
                writer.write("\n");
            }

            String[] headers = fields.stream().map(ExportField::getColumnLabel).toArray(String[]::new);

            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(headers)
                    .build();

            try (CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
                for (PaperExportData paper : papers) {
                    Object[] row = fields.stream()
                            .map(field -> resolveFieldValue(paper, field))
                            .toArray();
                    printer.printRecord(row);
                }
            }

        } catch (IOException e) {
            throw new UncheckedIOException("Lỗi khi tạo file CSV export", e);
        }

        return out.toByteArray();
    }

    /** Comment line (#...) không qua CSV escape, chỉ cần loại bỏ newline để không vỡ format. */
    private String escapeForCommentLine(String value) {
        if (value == null) return NOT_AVAILABLE;
        return value.replace("\n", " ").replace("\r", " ");
    }

    // ─────────────────────────────────────────────────────────
    // JSON RENDERING
    // ─────────────────────────────────────────────────────────

    private byte[] renderJson(List<PaperExportData> papers, List<ExportField> fields, ExportMetadata metadata) {
        try {
            ObjectNode root = objectMapper.createObjectNode();

            if (metadata != null) {
                root.set("metadata", objectMapper.valueToTree(metadata));
            }

            var papersArray = objectMapper.createArrayNode();
            for (PaperExportData paper : papers) {
                ObjectNode paperNode = objectMapper.createObjectNode();
                for (ExportField field : fields) {
                    String jsonKey = toJsonKey(field);
                    Object value = resolveFieldValue(paper, field);
                    paperNode.put(jsonKey, value.toString());
                }
                papersArray.add(paperNode);
            }
            root.set("papers", papersArray);

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(root);

        } catch (Exception e) {
            throw new UncheckedIOException("Lỗi khi tạo file JSON export", new IOException(e));
        }
    }

    private String toJsonKey(ExportField field) {
        // TITLE -> title, CITATION_COUNT -> citationCount (camelCase cho JSON)
        String[] parts = field.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        }
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────
    // FIELD VALUE RESOLUTION — gộp multi-value bằng " | ", N/A nếu thiếu
    // ─────────────────────────────────────────────────────────

    private String resolveFieldValue(PaperExportData paper, ExportField field) {
        Object rawValue = switch (field) {
            case TITLE -> paper.getTitle();
            case AUTHORS -> paper.getAuthors();
            case YEAR -> paper.getYear();
            case CITATION_COUNT -> paper.getCitationCount();
            case OPEN_ACCESS -> paper.getOpenAccess();
            case FIELD -> paper.getField();
            case DOMAIN -> paper.getDomain();
            case KEYWORD -> paper.getKeywords();
            case SUBFIELD -> paper.getSubfield();
            case TOPIC -> paper.getTopics();
            case INSTITUTION -> paper.getInstitutions();
            case COUNTRY -> paper.getCountries();
            case DOI -> paper.getDoi();
            case ABSTRACT -> paper.getAbstractText();
            case TYPE -> paper.getType();
        };

        return formatValue(rawValue);
    }

    private String formatValue(Object rawValue) {
        switch (rawValue) {
            case null -> {
                return NOT_AVAILABLE;
            }
            case List<?> list -> {
                if (list.isEmpty()) {
                    return NOT_AVAILABLE;
                }
                String joined = list.stream()
                        .filter(v -> v != null && !v.toString().isBlank())
                        .map(Object::toString)
                        .reduce((a, b) -> a + FIELD_SEPARATOR + b)
                        .orElse("");
                return joined.isBlank() ? NOT_AVAILABLE : joined;
            }
            case String str -> {
                return str.isBlank() ? NOT_AVAILABLE : str;
            }
            default -> {
            }
        }

        // Boolean, Integer, ...
        return rawValue.toString();
    }

    // ─────────────────────────────────────────────────────────
    // METADATA + FILE NAMING
    // ─────────────────────────────────────────────────────────

    private ExportMetadata buildMetadata(ExportReportRequest request, int actualPaperCount) {
        List<String> selectedFieldLabels = request.fields().stream()
                .map(ExportField::getColumnLabel)
                .toList();

        return new ExportMetadata(
                LocalDate.now(),
                request.searchQuery(),
                actualPaperCount,
                selectedFieldLabels,
                request.format().name()
        );
    }

    private String buildFileName(ExportFormat format) {
        String datePart = LocalDate.now().format(FILE_DATE_FORMAT);
        String extension = format == ExportFormat.CSV ? "csv" : "json";
        return "papers_report_" + datePart + "." + extension;
    }
}
