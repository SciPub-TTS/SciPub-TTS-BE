package com.brotherhood.scipubtts.openalexentity.service;

import java.util.Map;

public interface OpenAlexEntityService {
    Map<String, Object> getEntityDetail(String entityType, String entityId);

    Map<String, Object> getEntityTopWorks(
            String entityType,
            String entityId,
            Integer page,
            Integer perPage,
            String sort
    );
}
