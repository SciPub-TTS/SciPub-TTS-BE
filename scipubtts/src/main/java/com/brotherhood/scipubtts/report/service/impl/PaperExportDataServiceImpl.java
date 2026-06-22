package com.brotherhood.scipubtts.report.service.impl;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.report.dto.response.PaperExportData;
import com.brotherhood.scipubtts.report.service.PaperExportDataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation gọi trực tiếp OpenAlex API theo batch (filter ids).
 * ⚠️ NẾU PROJECT ĐÃ CÓ SẴN OpenAlex client/service (rất có thể đã có, theo
 * cấu trúc "package com.brotherhood.scipubtts" + tích hợp OpenAlex API đã
 * biết từ trước) — hãy THAY THẾ phần gọi RestClient dưới đây bằng cách gọi
 * lại service/client đó, để tránh trùng lặp logic gọi OpenAlex 2 nơi.
 * Class này được viết độc lập (không phụ thuộc cấu trúc nội bộ chưa rõ) để
 * bạn có ngay 1 bản chạy được, dễ thay thế sau.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaperExportDataServiceImpl implements PaperExportDataService {

    private static final int OPENALEX_BATCH_SIZE = 50; // OpenAlex cho phép filter OR theo nhiều id 1 lần gọi

    private final ObjectMapper objectMapper;
    private final OpenAlexClient openAlexClient;

    @Override
    public List<PaperExportData> fetchPapersForExport(List<String> paperIds) {

        if (paperIds == null || paperIds.isEmpty()) {
            return List.of();
        }

        Map<String, PaperExportData> resultById = new HashMap<>();

        // OpenAlex filter: openalex_id:W1|W2|W3...
        String filterValue = paperIds.stream()
                .map(this::normalizeOpenAlexId)
                .collect(Collectors.joining("|"));

        try {
            // 1. Build query params đúng kiểu Map<String,String> mà OpenAlexClient.get() yêu cầu
            Map<String, String> queryParams = Map.of(
                    "filter", "openalex_id:" + filterValue,
                    "per-page", String.valueOf(OPENALEX_BATCH_SIZE),
                    "select", OpenAlexClient.SELECT_FIELDS
            );

            // 2. Gọi đúng signature get(String path, Map<String,String> queryParams)
            Map<String, Object> responseMap = openAlexClient.get("/works", queryParams);

            // 3. Convert sang JsonNode để tái dùng các hàm mapping JsonNode hiện có
            JsonNode response = objectMapper.valueToTree(responseMap);

            // 4. ⚠️ ĐÃ SỬA: lặp qua "results" array, map TỪNG work, và PUT VÀO resultById.
            //    Bản trước gọi mapWorkToExportData(response) sai kiểu (nhận cả root node)
            //    và quan trọng nhất là KHÔNG put kết quả vào resultById → map luôn rỗng.
            if (response != null && response.has("results")) {
                for (JsonNode work : response.get("results")) {
                    PaperExportData data = mapWorkToExportData(work);
                    if (data.getOpenalexId() != null) {
                        resultById.put(data.getOpenalexId(), data);
                    }
                }
            }
        } catch (BusinessException e) {
            // Bắt các exception đã được phân loại chuẩn (Ví dụ: 404 -> OPENALEX_ENTITY_NOT_FOUND)
            log.error("Lỗi nghiệp vụ từ OpenAlex khi export data cho ID {}: {}", filterValue, e.getErrorCode());
            // Tùy chọn: ném tiếp hoặc bỏ qua để không sập toàn bộ luồng export của các bài khác
        } catch (Exception e) {
            // Bắt các lỗi hệ thống không lường trước được
            log.error("Lỗi không xác định khi gọi OpenAlex cho ID {}", filterValue, e);
        }

        // Giữ đúng thứ tự paperIds người dùng đã chọn; bỏ qua paper không tìm thấy
        List<PaperExportData> ordered = new ArrayList<>();
        for (String id : paperIds) {
            PaperExportData data = resultById.get(normalizeOpenAlexId(id));
            if (data != null) {
                ordered.add(data);
            } else {
                log.warn("Không tìm thấy dữ liệu OpenAlex cho paperId={}, bỏ qua khỏi report export", id);
            }
        }

        return ordered;
    }

    // ─────────────────────────────────────────────────────────
    // Mapping OpenAlex Work JSON → PaperExportData
    // ─────────────────────────────────────────────────────────

    private PaperExportData mapWorkToExportData(JsonNode work) {

        String openalexId = extractShortId(textOrNull(work, "id"));

        List<String> authors = new ArrayList<>();
        List<String> institutions = new ArrayList<>();
        List<String> countries = new ArrayList<>();

        if (work.has("authorships")) {
            for (JsonNode authorship : work.get("authorships")) {
                JsonNode author = authorship.get("author");
                if (author != null && author.has("display_name")) {
                    authors.add(author.get("display_name").asText());
                }
                if (authorship.has("institutions")) {
                    for (JsonNode inst : authorship.get("institutions")) {
                        String instName = textOrNull(inst, "display_name");
                        if (instName != null) institutions.add(instName);

                        String country = textOrNull(inst, "country_code");
                        if (country != null) countries.add(country);
                    }
                }
            }
        }

        List<String> topics = new ArrayList<>();
        List<String> keywords = new ArrayList<>();
        String primaryField = null;
        String primaryDomain = null;
        String primarySubfield = null;

        if (work.has("topics")) {
            for (JsonNode topic : work.get("topics")) {
                String topicName = textOrNull(topic, "display_name");
                if (topicName != null) topics.add(topicName);

                // Lấy field/domain/subfield từ topic có score cao nhất (topic đầu tiên,
                // OpenAlex luôn trả về topics đã sort theo score giảm dần)
                if (primaryField == null && topic.has("field")) {
                    primaryField = textOrNull(topic.get("field"), "display_name");
                }
                if (primaryDomain == null && topic.has("domain")) {
                    primaryDomain = textOrNull(topic.get("domain"), "display_name");
                }
                if (primarySubfield == null && topic.has("subfield")) {
                    primarySubfield = textOrNull(topic.get("subfield"), "display_name");
                }
            }
        }

        if (work.has("keywords")) {
            for (JsonNode kw : work.get("keywords")) {
                String kwText = textOrNull(kw, "display_name");
                if (kwText != null) keywords.add(kwText);
            }
        }

        Boolean openAccess = null;
        if (work.has("open_access") && work.get("open_access").has("is_oa")) {
            openAccess = work.get("open_access").get("is_oa").asBoolean();
        }

        Integer citationCount = work.has("cited_by_count") ? work.get("cited_by_count").asInt() : null;
        Integer year = work.has("publication_year") ? work.get("publication_year").asInt() : null;
        String doi = textOrNull(work, "doi");
        String type = textOrNull(work, "type");
        String title = textOrNull(work, "title");
        String abstractText = decodeAbstract(work.get("abstract_inverted_index"));

        return PaperExportData.builder()
                .openalexId(openalexId)
                .title(title)
                .authors(authors)
                .year(year)
                .citationCount(citationCount)
                .openAccess(openAccess)
                .field(primaryField)
                .domain(primaryDomain)
                .subfield(primarySubfield)
                .topics(topics)
                .keywords(keywords)
                .institutions(institutions)
                .countries(countries)
                .doi(doi)
                .abstractText(abstractText)
                .type(type)
                .build();
    }

    /**
     * OpenAlex trả "abstract_inverted_index" — cần decode ngược lại thành câu văn bình thường.
     */
    private String decodeAbstract(JsonNode invertedIndex) {
        if (invertedIndex == null || !invertedIndex.isEmpty()) {
            return null;
        }

        TreeMap<Integer, String> positionToWord = new TreeMap<>();
        invertedIndex.properties().forEach(entry -> {
            String word = entry.getKey();
            for (JsonNode posNode : entry.getValue()) {
                positionToWord.put(posNode.asInt(), word);
            }
        });

        return String.join(" ", positionToWord.values());
    }

    private String textOrNull(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).asText();
    }

    private String extractShortId(String fullOpenAlexUrl) {
        if (fullOpenAlexUrl == null) return null;
        return fullOpenAlexUrl.contains("/")
                ? fullOpenAlexUrl.substring(fullOpenAlexUrl.lastIndexOf('/') + 1)
                : fullOpenAlexUrl;
    }

    private String normalizeOpenAlexId(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.contains("/")) {
            trimmed = trimmed.substring(trimmed.lastIndexOf('/') + 1);
        }
        return trimmed.toUpperCase();
    }
}
