
package com.debarag.stockpulse.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IndianApiClient {

    private final RestClient restClient;

    public IndianApiClient(
            @Value("${indianapi.base-url}") String baseUrl,
            @Value("${indianapi.api-key}") String apiKey) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .build();
    }

    public IndianApiStockResponse getStockData(String stockName) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stock")
                        .queryParam("name", stockName)
                        .build())
                .retrieve()
                .body(IndianApiStockResponse.class);
    }
}
