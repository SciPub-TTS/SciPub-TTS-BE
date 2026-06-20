package com.brotherhood.scipubtts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.net.URI;

@Configuration
public class OpenAlexConfig {

  @Value("${openalex.base-url:https://api.openalex.org}")
  private String openAlexBaseUrl;

  @Bean
  public RestClient openAlexRestClient() {
    return RestClient.builder()
            .baseUrl("https://api.openalex.org")
            .defaultHeader("User-Agent", "ScipubTTS")
            .requestInterceptor((request, body, execution) -> {
              URI originalUri = request.getURI();
              String separator = originalUri.getQuery() == null ? "?" : "&";
              URI newUri = URI.create(originalUri + separator + "api_key=" + "");
              return execution.execute(new HttpRequestWrapper(request) {
                @Override
                public URI getURI() { return newUri; }
              }, body);
            })
            .baseUrl(openAlexBaseUrl)
            .defaultHeader(HttpHeaders.USER_AGENT, "ScipubTTS")
            .build();
  }
}
