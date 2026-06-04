package com.brotherhood.scipubtts.detailpaper.service;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PaperDetailServiceImpl implements PaperDetailService {

    // Root-level fields used by the paper detail page.
    private static final String WORK_DETAIL_SELECT_FIELDS =
            "id,title,doi,publication_year,publication_date,language,type,open_access,primary_location,best_oa_location,authorships,topics,primary_topic,keywords,cited_by_count,citation_normalized_percentile,fwci,referenced_works_count,related_works,locations_count,biblio,ids,apc_list,apc_paid,has_content,content_urls,indexed_in,counts_by_year,is_retracted,abstract_inverted_index";

    private final OpenAlexClient openAlexClient;

    public PaperDetailServiceImpl(OpenAlexClient openAlexClient) {
        this.openAlexClient = openAlexClient;
    }

    @Override
    public Map<String, Object> getWorkDetail(String workId) {
        if (!StringUtils.hasText(workId)) {
            throw new BusinessException(ErrorCode.OPENALEX_ENTITY_NOT_FOUND);
        }

        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("select", WORK_DETAIL_SELECT_FIELDS);

        return openAlexClient.get("/works/" + workId.trim(), queryParams);
    }
}
