package com.brotherhood.scipubtts.detailpaper.service;

import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class PaperDetailServiceImpl implements PaperDetailService {

    private final OpenAlexClient openAlexClient;
    private final PaperDetailOpenAlexQueryFactory paperDetailOpenAlexQueryFactory;

    public PaperDetailServiceImpl(
            OpenAlexClient openAlexClient,
            PaperDetailOpenAlexQueryFactory paperDetailOpenAlexQueryFactory
    ) {
        this.openAlexClient = openAlexClient;
        this.paperDetailOpenAlexQueryFactory = paperDetailOpenAlexQueryFactory;
    }

    @Override
    public Map<String, Object> getWorkDetail(String workId) {
        return openAlexClient.get(
                paperDetailOpenAlexQueryFactory.buildWorkDetailPath(workId),
                paperDetailOpenAlexQueryFactory.buildWorkDetailQueryParams()
        );
    }
}
