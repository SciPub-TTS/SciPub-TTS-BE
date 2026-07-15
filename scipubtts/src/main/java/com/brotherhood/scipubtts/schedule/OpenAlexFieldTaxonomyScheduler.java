package com.brotherhood.scipubtts.schedule;

import com.brotherhood.scipubtts.admin.service.OpenAlexFieldTaxonomySyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenAlexFieldTaxonomyScheduler {

    private final OpenAlexFieldTaxonomySyncService openAlexFieldTaxonomySyncService;

    public void syncFieldTaxonomyMonthly() {
        openAlexFieldTaxonomySyncService.syncFieldTaxonomy();
    }
}
