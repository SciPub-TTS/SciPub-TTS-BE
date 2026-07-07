package com.brotherhood.scipubtts.schedule;

import com.brotherhood.scipubtts.feed.service.ResearchFeedSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResearchFeedScheduler {

    private final ResearchFeedSyncService researchFeedSyncService;

    public void syncResearchFeedDaily() {
        researchFeedSyncService.syncDailyFeed();
    }

}
