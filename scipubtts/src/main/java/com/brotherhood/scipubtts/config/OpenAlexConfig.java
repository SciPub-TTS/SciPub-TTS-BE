package com.brotherhood.scipubtts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenAlexConfig {

  @Value("${openalex.base-url:https://api.openalex.org}")
  private String openAlexBaseUrl;

  @Bean
  public RestClient openAlexRestClient(RestClient.Builder restClientBuilder) {
    return restClientBuilder
            .baseUrl(openAlexBaseUrl)
            .defaultHeader(HttpHeaders.USER_AGENT, "ScipubTTS")
            .build();
  }
}
