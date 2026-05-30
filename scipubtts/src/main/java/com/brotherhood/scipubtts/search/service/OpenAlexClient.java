package com.brotherhood.scipubtts.search.service;

import com.fasterxml.jackson.databind.JsonNode;
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

    public JsonNode get(String path, Map<String, String> queryParams) {
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
            return objectMapper.readTree(responseBody);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot parse OpenAlex response", exception);
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

        throw new IllegalStateException("OpenAlex request failed after retries", lastException);
    }

    private boolean isRetryableStatus(int statusCode) {
        return statusCode == 429 || statusCode >= 500;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry interrupted", exception);
        }
    }
}

/*
SEARCH_FILE_NOTE
Syntax su dung:
- @Component, RestClient, UriComponentsBuilder.
- Retry with exponential backoff bang vong for + sleep.
File nay lam gi:
- Goi API OpenAlex tu BE.
Flow chay:
- Service dua path/params -> client build URL -> call GET -> parse JSON -> tra JsonNode.
*/

