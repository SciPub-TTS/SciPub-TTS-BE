package com.brotherhood.scipubtts.common.openalex;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class OpenAlexClient {

    public static final int MAX_RETRIES = 5;
    private static final long INITIAL_BACKOFF_MILLIS = 500L;


    public static final String SELECT_FIELDS = "id,title,publication_year,cited_by_count,open_access,authorships,topics,keywords,doi,type,abstract_inverted_index";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${openalex.api-key:}")
    private String openAlexApiKey;

    @Value("${openalex.mailto:}")
    private String openAlexMailto;

    public OpenAlexClient(
            @Qualifier("openAlexRestClient") RestClient restClient,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> get(String path, Map<String, String> queryParams) {
        String responseBody = performGetWithRetry(path, buildQueryParams(queryParams));

        try {
            return objectMapper.readValue(responseBody, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.OPENALEX_PARSE_ERROR);
        }
    }

    private Map<String, String> buildQueryParams(Map<String, String> queryParams) {
        Map<String, String> finalQueryParams = new LinkedHashMap<>();

        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                String value = entry.getValue();
                if (StringUtils.hasText(value)) {
                    finalQueryParams.put(entry.getKey(), value.trim());
                }
            }
        }

        if (StringUtils.hasText(openAlexApiKey)) {
            finalQueryParams.put("api_key", openAlexApiKey);
        }

        if (StringUtils.hasText(openAlexMailto)) {
            finalQueryParams.put("mailto", openAlexMailto);
        }

        return finalQueryParams;
    }

    private String performGetWithRetry(String path, Map<String, String> queryParams) {
        long backoffMillis = INITIAL_BACKOFF_MILLIS;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return executeGet(path, queryParams);
            } catch (RestClientResponseException exception) {
                if (exception.getStatusCode().value() == 404) {
                    throw new BusinessException(ErrorCode.OPENALEX_ENTITY_NOT_FOUND);
                }

                if (!isRetryableStatus(exception.getStatusCode().value()) || attempt == MAX_RETRIES) {
                    throw exception;
                }
            } catch (RestClientException exception) {
                if (attempt == MAX_RETRIES) {
                    throw exception;
                }
            }

            sleep(backoffMillis);
            backoffMillis *= 2;
        }

        throw new BusinessException(ErrorCode.OPENALEX_REQUEST_FAILED);
    }

    private String executeGet(String path, Map<String, String> queryParams) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(path);

                    for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                        uriBuilder.queryParam(entry.getKey(), entry.getValue());
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .body(String.class);
    }

    private boolean isRetryableStatus(int statusCode) {
        return statusCode == 429 || statusCode >= 500;
    }

    private void sleep(long delayMillis) {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.RETRY_INTERRUPTED);
        }
    }

    public long getWorksCount() {
        return fetchCount("/works?per-page=1");
    }

    public long getAuthorsCount() {
        return fetchCount("/authors?per-page=1");
    }

    public long getTopicsCount() {
        return fetchCount("/topics?per-page=1");
    }

    public long getFieldsCount() {
        return fetchCount("/fields?per-page=1");
    }

    private long fetchCount(String uri) {

        JsonNode response = restClient
                .get()
                .uri(uri)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            return 0;
        }

        return response
                .path("meta")
                .path("count")
                .asLong();
    }
}
