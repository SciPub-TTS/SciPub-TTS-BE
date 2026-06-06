package com.brotherhood.scipubtts.canvas.service;

import java.util.Map;

public interface CanvasService {
    Map<String, Object> getEntityDetail(String entityType, String entityId);

    Map<String, Object> getEntityTopWorks(
            String entityType,
            String entityId,
            Integer page,
            Integer perPage,
            String sort
    );
}
