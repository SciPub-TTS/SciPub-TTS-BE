package com.brotherhood.scipubtts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenAlexConfig {

  @Bean
  public RestClient openAlexClient() {
    return RestClient.builder()
            .baseUrl("https://api.openalex.org")
            .defaultHeader("User-Agent", "ScipubTTS")
            .build();
  }
}