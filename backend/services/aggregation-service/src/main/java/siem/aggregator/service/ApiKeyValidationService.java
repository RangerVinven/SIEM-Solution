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
        @Value("${account.service.url}") String accountServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(accountServiceUrl).build();
    }

    @Cacheable(value = "apiKeys", key = "#apiKey", unless = "#result == null")
    public String getSchoolId(String apiKey) {
        try {
            return restClient.get()
                .uri("/api/account/schools/api-keys/{key}", apiKey)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new InvalidApiKeyException();
                })
                .body(String.class);
        } catch (InvalidApiKeyException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static class InvalidApiKeyException extends RuntimeException {}
}
