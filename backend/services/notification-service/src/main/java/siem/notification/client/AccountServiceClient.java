package siem.notification.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

@Component
public class AccountServiceClient {

    private final RestClient restClient;

    public AccountServiceClient(@Value("${account.service.url}") String url) {
        this.restClient = RestClient.builder().baseUrl(url).build();
    }

    public List<Map<String, Object>> getSchoolUsers(String schoolId) {
        Map[] users = restClient.get()
                .uri("/schools/{id}/users", schoolId)
                .retrieve()
                .body(Map[].class);
        return users != null ? Arrays.asList(users) : List.of();
    }
}
