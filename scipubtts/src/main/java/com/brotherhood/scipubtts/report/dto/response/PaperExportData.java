package com.brotherhood.scipubtts.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Model trung gian — đại diện 1 paper sau khi đã lấy đủ dữ liệu từ OpenAlex
 * (hoặc cache nội bộ), TRƯỚC khi render ra CSV/JSON.
 * Các field danh sách (authors, institutions, countries, topics, keywords)
 * được giữ nguyên dạng List<String> ở tầng này; việc gộp bằng " | " chỉ xảy ra
 * tại bước render CSV/JSON (tầng View), không xử lý sớm ở đây để JSON export
 * vẫn có thể trả về array thật nếu cần mở rộng sau này.
 */
@Getter
@Builder
public class  PaperExportData {

    private String openalexId;
    private String title;
    private List<String> authors;
    private Integer year;
    private Integer citationCount;
    private Boolean openAccess;
    private String field;
    private String domain;
    private List<String> keywords;
    private String subfield;
    private List<String> topics;
    private List<String> institutions;
    private List<String> countries;
    private String doi;
    private String abstractText;
    private String type;
}
