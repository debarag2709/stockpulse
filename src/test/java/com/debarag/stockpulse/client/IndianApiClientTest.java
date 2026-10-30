
package com.debarag.stockpulse.client;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class IndianApiClientTest {

    private HttpServer server;
    private IndianApiClient client;

    private final AtomicReference<String> capturedApiKey =
            new AtomicReference<>();

    private final AtomicReference<String> capturedQuery =
            new AtomicReference<>();

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(
                new InetSocketAddress("localhost", 0), 0);

        server.createContext("/stock", exchange -> {
            capturedApiKey.set(
                    exchange.getRequestHeaders().getFirst("x-api-key"));

            String query = exchange.getRequestURI().getRawQuery();
            capturedQuery.set(query);

            String json = """
                    {
                      "companyName": "Tata Steel",
                      "exchangeCodeNse": "TATASTEEL",
                      "exchangeCodeBse": "500470",
                      "currentPrice": {
                        "BSE": "175.74",
                        "NSE": "175.50"
                      },
                      "percentChange": "-0.37",
                      "yearHigh": "224.40",
                      "yearLow": "160.00"
                    }
                    """;

            byte[] body = json.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                    "Content-Type", "application/json");

            exchange.sendResponseHeaders(200, body.length);

            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });

        server.start();

        client = new IndianApiClient(
                "http://localhost:" + server.getAddress().getPort(),
                "test-api-key");
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void getStockDataParsesResponseAndSendsApiKey() {
        IndianApiStockResponse response =
                client.getStockData("Tata Steel");

        assertNotNull(response);
        assertEquals("Tata Steel", response.companyName());
        assertEquals("TATASTEEL", response.exchangeCodeNse());
        assertEquals("500470", response.exchangeCodeBse());
        assertEquals("175.50", response.currentPrice().get("NSE"));
        assertEquals("175.74", response.currentPrice().get("BSE"));
        assertEquals("-0.37", response.percentChange());
        assertEquals("224.40", response.yearHigh());
        assertEquals("160.00", response.yearLow());

        assertEquals("test-api-key", capturedApiKey.get());
        assertEquals("name=Tata%20Steel", capturedQuery.get());
    }
}
