package com.brotherhood.scipubtts.search.service;

public final class SearchConstants {

    public static final int FILTER_OPTION_LIMIT = 100;
    public static final int WORKS_PER_PAGE_LIMIT = 100;
    public static final int ENTITY_SCOPE_FETCH_PER_PAGE = 100;
    public static final String SCOPED_DOMAIN_ID = "3";
    public static final String SCOPED_FIELD_IDS = "17|22";
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_WORKS_PER_PAGE = 10;
    public static final int DEFAULT_RECENT_SEARCH_LIMIT = 5;
    public static final int MAX_RECENT_SEARCH_LIMIT = 20;
    public static final int MIN_YEAR = 100;
    public static final int MIN_CITATION = 0;
    public static final String TOPICS_SCOPE_FILTER =
            "domain.id:" + SCOPED_DOMAIN_ID + ",field.id:" + SCOPED_FIELD_IDS;
    public static final String WORKS_SCOPE_FILTER =
            "primary_topic.domain.id:" + SCOPED_DOMAIN_ID
                    + ",primary_topic.field.id:" + SCOPED_FIELD_IDS;

    public static final String WORKS_SELECT_FIELDS =
            "id,display_name,abstract_inverted_index,doi,publication_year,cited_by_count,type,primary_topic,primary_location,authorships,open_access,best_oa_location,has_content,keywords";

    private SearchConstants() {
    }
}
