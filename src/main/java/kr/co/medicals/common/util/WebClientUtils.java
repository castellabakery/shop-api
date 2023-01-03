package kr.co.medicals.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

@Slf4j
@Component
public class WebClientUtils {

    private static final int TIMEOUT_SEC = 10;

    private WebClient webClient;

    public WebClientUtils(WebClient.Builder builder) {
        webClient = builder.build();
    }

    public <T> Mono<ResponseEntity<ApiResponse>> requestWebClient(HttpMethod httpMethod, String uri, Object requestBody) {
        return webClient
                .method(httpMethod)
                .uri(uri)
                .bodyValue(requestBody)
                .retrieve()
                .toEntity(ApiResponse.class)
                .timeout(Duration.ofSeconds(TIMEOUT_SEC));
    }

    public <T> Mono<ResponseEntity<ApiResponse>> requestWebClient(HttpMethod httpMethod, URI uri, Object requestBody) {
        return webClient
                .method(httpMethod)
                .uri(uri)
                .bodyValue(requestBody)
                .retrieve()
                .toEntity(ApiResponse.class)
                .timeout(Duration.ofSeconds(TIMEOUT_SEC));
    }

    public <T> Mono<ResponseEntity<ApiResponse>> requestWebClient(HttpMethod httpMethod, URI uri) {
        return webClient
                .method(httpMethod)
                .uri(uri)
                .retrieve()
                .toEntity(ApiResponse.class)
                .timeout(Duration.ofSeconds(TIMEOUT_SEC));
    }

    public Mono<ResponseEntity<String>> requestWebClientReturnString(HttpMethod httpMethod, URI uri) {
        return webClient
                .method(httpMethod)
                .uri(uri)
                .retrieve()
                .toEntity(String.class)
                .timeout(Duration.ofSeconds(TIMEOUT_SEC));
    }

}
