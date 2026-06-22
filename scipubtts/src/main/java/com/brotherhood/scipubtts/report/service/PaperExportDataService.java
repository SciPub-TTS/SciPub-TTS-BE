package com.brotherhood.scipubtts.report.service;

import com.brotherhood.scipubtts.report.dto.response.PaperExportData;

import java.util.List;

/**
 * Interface trung gian để lấy dữ liệu Paper phục vụ Export.
 * ⚠️ LƯU Ý TÍCH HỢP:
 * Project hiện tại đã có client/service gọi OpenAlex API (theo cấu trúc module
 * "paper" / OpenAlex integration đã tồn tại). Bạn cần implement interface này
 * bằng cách map từ response OpenAlex (hoặc cache nội bộ openalex_entity_cache)
 * sang PaperExportData.
 * Ví dụ implement (PaperExportDataServiceImpl) sẽ:
 *   1. Gọi paperService.getWorksByIds(paperIds) hoặc tương đương đã có sẵn.
 *   2. Map từng Work (OpenAlex) → PaperExportData:
 *      - title              ← work.getTitle()
 *      - authors            ← work.getAuthorships().stream().map(a -> a.getAuthor().getDisplayName())
 *      - year               ← work.getPublicationYear()
 *      - citationCount      ← work.getCitedByCount()
 *      - openAccess         ← work.getOpenAccess().getIsOa()
 *      - field/domain/subfield/topic ← work.getTopics() (OpenAlex topic hierarchy)
 *      - keywords           ← work.getKeywords()
 *      - institutions       ← work.getAuthorships().stream().flatMap(institutions)
 *      - countries          ← work.getAuthorships().stream().flatMap(institution countries)
 *      - doi                ← work.getDoi()
 *      - abstractText       ← work.getAbstractInverted() đã được decode thành text thường
 *      - type               ← work.getType()
 * Nếu một paperId không tìm thấy trên OpenAlex, BỎ QUA (không throw lỗi toàn bộ
 * request) — phần "Xử lý dữ liệu thiếu" được áp dụng ở mức field (N/A),
 * không áp dụng ở mức bỏ nguyên cả paper, nhưng nếu cả paper không tồn tại,
 * tốt nhất loại nó khỏi danh sách kết quả cuối + log lại để debug.
 */
public interface PaperExportDataService {

    /**
     * Lấy dữ liệu đầy đủ của các paper theo danh sách OpenAlex Work ID,
     * theo ĐÚNG THỨ TỰ paperIds được truyền vào (để giữ thứ tự người dùng chọn).
     */
    List<PaperExportData> fetchPapersForExport(List<String> paperIds);
}
