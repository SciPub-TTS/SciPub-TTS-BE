package com.brotherhood.scipubtts.feed.service.impl;

import com.brotherhood.scipubtts.feed.model.FeedDraft;
import com.brotherhood.scipubtts.feed.repository.ResearchFeedRepository;
import com.brotherhood.scipubtts.feed.service.FeedPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FeedPersistenceServiceImpl implements FeedPersistenceService {

    private final ResearchFeedRepository researchFeedRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int saveFeedDrafts(Collection<FeedDraft> drafts) {
        return researchFeedRepository.batchInsertDoNothing(drafts);
    }
}