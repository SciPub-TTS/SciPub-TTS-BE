package com.brotherhood.scipubtts.canvas.service;

import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class CanvasServiceImpl implements CanvasService {

    private final OpenAlexClient openAlexClient;
    private final CanvasOpenAlexQueryFactory canvasOpenAlexQueryFactory;

    public CanvasServiceImpl(
            OpenAlexClient openAlexClient,
            CanvasOpenAlexQueryFactory canvasOpenAlexQueryFactory
    ) {
        this.openAlexClient = openAlexClient;
        this.canvasOpenAlexQueryFactory = canvasOpenAlexQueryFactory;
    }

    @Override
    public Map<String, Object> getEntityDetail(String entityType, String entityId) {
        CanvasEntityType resolvedType = CanvasEntityType.fromValue(entityType);

        return openAlexClient.get(
                canvasOpenAlexQueryFactory.buildEntityDetailPath(resolvedType, entityId),
                Map.of()
        );
    }

    @Override
    public Map<String, Object> getEntityTopWorks(
            String entityType,
            String entityId,
            Integer page,
            Integer perPage,
            String sort
    ) {
        CanvasEntityType resolvedType = CanvasEntityType.fromValue(entityType);

        return openAlexClient.get(
                "/works",
                canvasOpenAlexQueryFactory.buildTopWorksQueryParams(
                        resolvedType,
                        entityId,
                        page,
                        perPage,
                        sort
                )
        );
    }
}
