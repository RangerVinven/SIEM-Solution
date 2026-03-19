package siem.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class ApiKeyValidationService {

    private final RestClient restClient;

    public ApiKeyValidationService(
        RestClient.Builder restClientBuilder,
        @Value("${account.service.url}") String accountServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(accountServiceUrl).build();
    }

    @Cacheable(value = "apiKeys", key = "#apiKey", unless = "#result == false")
    public boolean isValidApiKey(String apiKey) {
        try {
            restClient.get()
                .uri("/organisations/api-keys/{key}", apiKey)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new InvalidApiKeyException();
                })
                .toBodilessEntity();

            return true;
        } catch (InvalidApiKeyException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static class InvalidApiKeyException extends RuntimeException {}
}
