package com.proxy;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Bean
    public CloseableHttpClient httpClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(50);
        connectionManager.setDefaultMaxPerRoute(10);

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .evictIdleConnections(TimeValue.ofSeconds(30))
                .build();
    }
}
