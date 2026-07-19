package com.brotherhood.scipubtts.schedule;

import com.brotherhood.scipubtts.admin.service.OpenAlexFieldTaxonomySyncService;
import com.brotherhood.scipubtts.common.openalex.logging.OpenAlexCallContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenAlexFieldTaxonomyScheduler {

    private static final String JOB_TYPE = "OPENALEX_TAXONOMY_SYNC";

    private final OpenAlexFieldTaxonomySyncService openAlexFieldTaxonomySyncService;

    public void syncFieldTaxonomyMonthly() {
        OpenAlexCallContext.runAsSystemJob(null, JOB_TYPE, openAlexFieldTaxonomySyncService::syncFieldTaxonomy);
    }
}
