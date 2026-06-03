package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Component
public class OpenAlexClient {

    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_BACKOFF_MILLIS = 500L;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${openalex.base-url:https://api.openalex.org}")
    private String openAlexBaseUrl;

    @Value("${openalex.api-key:}")
    private String openAlexApiKey;

    @Value("${openalex.mailto:}")
    private String openAlexMailto;

    public OpenAlexClient(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> get(String path, Map<String, String> queryParams) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(openAlexBaseUrl + path);

        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                uriBuilder.queryParam(entry.getKey(), entry.getValue());
            }
        }

        if (StringUtils.hasText(openAlexApiKey)) {
            uriBuilder.queryParam("api_key", openAlexApiKey);
        }

        if (StringUtils.hasText(openAlexMailto)) {
            uriBuilder.queryParam("mailto", openAlexMailto);
        }

        URI uri = uriBuilder.build().encode().toUri();
        String responseBody = performGetWithRetry(uri);

        try {
            return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.OPENALEX_PARSE_ERROR);
        }
    }

    private String performGetWithRetry(URI uri) {
        long backoff = INITIAL_BACKOFF_MILLIS;
        RestClientException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return restClient.get()
                        .uri(uri)
                        .retrieve()
                        .body(String.class);
            } catch (RestClientResponseException exception) {
                lastException = exception;

                if (!isRetryableStatus(exception.getStatusCode().value()) || attempt == MAX_RETRIES) {
                    throw exception;
                }
            } catch (RestClientException exception) {
                lastException = exception;

                if (attempt == MAX_RETRIES) {
                    throw exception;
                }
            }

            sleep(backoff);
            backoff *= 2;
        }

        throw new BusinessException(ErrorCode.OPENALEX_REQUEST_FAILED);
    }

    private boolean isRetryableStatus(int statusCode) {
        return statusCode == 429 || statusCode >= 500;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.RETRY_INTERRUPTED);
        }
    }
}

