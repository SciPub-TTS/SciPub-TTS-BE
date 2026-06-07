package com.brotherhood.scipubtts.openalexentity.service;

import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class OpenAlexEntityServiceImpl implements OpenAlexEntityService {

    private final OpenAlexClient openAlexClient;
    private final OpenAlexEntityQueryFactory openAlexEntityQueryFactory;

    public OpenAlexEntityServiceImpl(
            OpenAlexClient openAlexClient,
            OpenAlexEntityQueryFactory openAlexEntityQueryFactory
    ) {
        this.openAlexClient = openAlexClient;
        this.openAlexEntityQueryFactory = openAlexEntityQueryFactory;
    }

    @Override
    public Map<String, Object> getEntityDetail(String entityType, String entityId) {
        OpenAlexEntityType resolvedType = OpenAlexEntityType.fromValue(entityType);

        return openAlexClient.get(
                openAlexEntityQueryFactory.buildEntityDetailPath(resolvedType, entityId),
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
        OpenAlexEntityType resolvedType = OpenAlexEntityType.fromValue(entityType);

        return openAlexClient.get(
                "/works",
                openAlexEntityQueryFactory.buildTopWorksQueryParams(
                        resolvedType,
                        entityId,
                        page,
                        perPage,
                        sort
                )
        );
    }
}
