package com.brotherhood.scipubtts.feed.service;

import com.brotherhood.scipubtts.feed.model.FeedDraft;

import java.util.Collection;

public interface FeedPersistenceService {

    public int saveFeedDrafts(Collection<FeedDraft> drafts);
}
