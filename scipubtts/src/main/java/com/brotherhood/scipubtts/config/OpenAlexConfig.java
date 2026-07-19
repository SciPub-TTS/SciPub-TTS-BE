package com.brotherhood.scipubtts.config;

import com.brotherhood.scipubtts.common.openalex.logging.OpenAlexLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenAlexConfig {

  @Value("${openalex.base-url:https://api.openalex.org}")
  private String openAlexBaseUrl;

  @Value("${openalex.api-key:}")
  private String openAlexApiKey;

  @Bean
  public RestClient openAlexRestClient(OpenAlexLoggingInterceptor openAlexLoggingInterceptor) {
    var builder = RestClient.builder()
            .baseUrl(openAlexBaseUrl)
            .defaultHeader(HttpHeaders.USER_AGENT, "ScipubTTS")
            .requestInterceptor(openAlexLoggingInterceptor);

    if (openAlexApiKey != null && !openAlexApiKey.isBlank()) {
      builder = builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAlexApiKey);
    }

    return builder.build();
  }
}
