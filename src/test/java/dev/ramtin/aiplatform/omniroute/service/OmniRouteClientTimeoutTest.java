package dev.ramtin.aiplatform.omniroute.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.ramtin.aiplatform.config.OmniRouteProperties;
import dev.ramtin.aiplatform.exception.AIProviderException;
import dev.ramtin.aiplatform.omniroute.request.OmniRouteMessage;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteModelsResponse;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OmniRouteClientTimeoutTest {
    @ParameterizedTest
    @EnumSource(Endpoint.class)
    void delayedHeaders_timeOutAndPreserveProviderErrorTranslation(Endpoint endpoint) throws Exception {
        assertTimeout(endpoint, ResponseMode.DELAYED_HEADERS);
    }

    @ParameterizedTest
    @EnumSource(Endpoint.class)
    void stalledBody_timesOutAndPreservesProviderErrorTranslation(Endpoint endpoint) throws Exception {
        assertTimeout(endpoint, ResponseMode.STALLED_BODY);
    }

    @ParameterizedTest
    @EnumSource(Endpoint.class)
    void normalResponse_succeedsWithProductionConstructor(Endpoint endpoint) throws Exception {
        try (LoopbackServer server = new LoopbackServer(endpoint, ResponseMode.NORMAL)) {
            OmniRouteClientImpl client = server.client(Duration.ofSeconds(5));

            Object response = assertTimeoutPreemptively(Duration.ofSeconds(8), () -> endpoint.invoke(client));

            if (endpoint == Endpoint.MODELS) {
                OmniRouteModelsResponse models = assertInstanceOf(OmniRouteModelsResponse.class, response);
                assertEquals("combo/test", models.getData().get(0).getId());
                assertEquals("Test model", models.getData().get(0).getName());
            } else {
                OmniRouteResponse completion = assertInstanceOf(OmniRouteResponse.class, response);
                assertEquals("response-1", completion.getId());
                assertEquals("combo/test", completion.getModel());
            }
            server.assertRequest();
        }
    }

    @ParameterizedTest
    @EnumSource(Endpoint.class)
    void httpError_preservesProviderErrorTranslation(Endpoint endpoint) throws Exception {
        try (LoopbackServer server = new LoopbackServer(endpoint, ResponseMode.HTTP_ERROR)) {
            OmniRouteClientImpl client = server.client(Duration.ofSeconds(5));

            AIProviderException exception = assertTimeoutPreemptively(Duration.ofSeconds(8),
                    () -> assertThrows(AIProviderException.class, () -> endpoint.invoke(client)));

            assertEquals(endpoint.errorMessage, exception.getMessage());
            assertInstanceOf(RestClientException.class, exception.getCause());
            server.assertRequest();
        }
    }

    private void assertTimeout(Endpoint endpoint, ResponseMode mode) throws Exception {
        try (LoopbackServer server = new LoopbackServer(endpoint, mode)) {
            OmniRouteClientImpl client = server.client(Duration.ofMillis(750));

            AIProviderException exception = assertTimeoutPreemptively(Duration.ofSeconds(8),
                    () -> assertThrows(AIProviderException.class, () -> endpoint.invoke(client)));

            assertEquals(endpoint.errorMessage, exception.getMessage());
            assertInstanceOf(RestClientException.class, exception.getCause());
            assertTrue(server.responseBlocked.await(1, TimeUnit.SECONDS),
                    "Server must reach the intended stalled response phase");
            server.assertRequest();
        }
    }

    private enum Endpoint {
        MODELS("/v1/models", "GET", "AI provider model-list request failed",
                "{\"data\":[{\"id\":\"combo/test\",\"name\":\"Test model\"}]}"),
        CHAT("/v1/chat/completions", "POST", "AI provider request failed",
                "{\"id\":\"response-1\",\"model\":\"combo/test\",\"choices\":[],"
                        + "\"usage\":{\"prompt_tokens\":0,\"completion_tokens\":0,\"total_tokens\":0}}");

        private final String path;
        private final String method;
        private final String errorMessage;
        private final String response;

        Endpoint(String path, String method, String errorMessage, String response) {
            this.path = path;
            this.method = method;
            this.errorMessage = errorMessage;
            this.response = response;
        }

        Object invoke(OmniRouteClientImpl client) {
            return this == MODELS ? client.getModels()
                    : client.sendMessage("combo/test", List.of(new OmniRouteMessage("user", "hello")));
        }
    }

    private enum ResponseMode {
        DELAYED_HEADERS, STALLED_BODY, NORMAL, HTTP_ERROR
    }

    private static class LoopbackServer implements AutoCloseable {
        private final HttpServer server;
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final CountDownLatch releaseResponse = new CountDownLatch(1);
        private final CountDownLatch responseBlocked = new CountDownLatch(1);
        private final AtomicInteger requests = new AtomicInteger();
        private final Endpoint endpoint;
        private volatile String requestMethod;
        private volatile String requestPath;
        private volatile String authorization;
        private volatile String protocol;
        private volatile String requestBody;

        LoopbackServer(Endpoint endpoint, ResponseMode mode) throws IOException {
            this.endpoint = endpoint;
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.setExecutor(executor);
            server.createContext("/", exchange -> handle(exchange, mode));
            server.start();
        }

        OmniRouteClientImpl client(Duration readTimeout) {
            OmniRouteProperties properties = new OmniRouteProperties();
            properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
            properties.setApiKey("test-key");
            properties.setReadTimeout(readTimeout);
            return new OmniRouteClientImpl(properties);
        }

        private void handle(HttpExchange exchange, ResponseMode mode) throws IOException {
            try (exchange) {
                requests.incrementAndGet();
                requestMethod = exchange.getRequestMethod();
                requestPath = exchange.getRequestURI().getPath();
                authorization = exchange.getRequestHeaders().getFirst("Authorization");
                protocol = exchange.getProtocol();
                requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                if (mode == ResponseMode.DELAYED_HEADERS) {
                    responseBlocked.countDown();
                    releaseResponse.await();
                    return;
                }
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                if (mode == ResponseMode.STALLED_BODY) {
                    exchange.sendResponseHeaders(200, 0);
                    exchange.getResponseBody().write("{\"".getBytes(StandardCharsets.UTF_8));
                    exchange.getResponseBody().flush();
                    responseBlocked.countDown();
                    releaseResponse.await();
                    return;
                }
                byte[] body = endpoint.response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(mode == ResponseMode.HTTP_ERROR ? 503 : 200, body.length);
                exchange.getResponseBody().write(body);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }

        void assertRequest() {
            assertEquals(1, requests.get(), "Requests must not be retried");
            assertEquals(endpoint.method, requestMethod);
            assertEquals(endpoint.path, requestPath);
            assertEquals("Bearer test-key", authorization);
            assertEquals("HTTP/1.1", protocol);
            if (endpoint == Endpoint.CHAT) {
                assertTrue(requestBody.contains("\"model\":\"combo/test\""));
                assertTrue(requestBody.contains("\"content\":\"hello\""));
            }
        }

        @Override
        public void close() throws InterruptedException {
            releaseResponse.countDown();
            server.stop(0);
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "Loopback executor must stop");
        }
    }
}
