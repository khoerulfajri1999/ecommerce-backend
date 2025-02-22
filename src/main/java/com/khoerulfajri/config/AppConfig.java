package com.khoerulfajri.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
//                    headers.add(HttpHeaders.AUTHORIZATION, "Bearer" + "YOUR_TOKEN_HERE");
                })
                .build();
    }
}
