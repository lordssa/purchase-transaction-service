package com.wex.purchase.config;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    private static final int READ_TIMEOUT_DURATION = 5;
    private static final int CONNECT_TIMEOUT_DURATION = 3;

    @Bean
    RestClient.Builder restClientBuilder() {
        var executor = Executors.newVirtualThreadPerTaskExecutor();

        HttpClient httpClient = HttpClient.newBuilder()
                .executor(executor)
                .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_DURATION))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(READ_TIMEOUT_DURATION));

        return RestClient.builder().requestFactory(requestFactory);
    }
}

