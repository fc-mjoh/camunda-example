package com.reply.camunda.delegate;

import com.reply.camunda.exception.RestConnectorException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class RestServiceDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        RestClient restClient = RestClient.builder().build();
        String url = (String) execution.getVariable("restUrl");
        String method = (String) execution.getVariable("httpMethod");

        if (url == null || url.isBlank()) {
            throw new RestConnectorException("restUrl ist nicht konfiguriert");
        }

        try {
            ResponseEntity<String> response = switch (method != null ? method.toUpperCase() : "GET") {

                case "POST" -> {
                    String body = (String) execution.getVariable("requestBody");
                    yield restClient.post()
                            .uri(url)
                            .body(body)
                            .retrieve()
                            .toEntity(String.class);
                }

                case "PUT" -> {
                    String body = (String) execution.getVariable("requestBody");
                    yield restClient.put()
                            .uri(url)
                            .body(body)
                            .retrieve()
                            .toEntity(String.class);
                }

                case "DELETE" -> restClient.delete()
                        .uri(url)
                        .retrieve()
                        .toEntity(String.class);

                default -> restClient.get()
                        .uri(url)
                        .retrieve()
                        .toEntity(String.class);
            };

            execution.setVariableLocal("responseBody", response.getBody());
            execution.setVariableLocal("responseStatus", response.getStatusCode().value());
        } catch (RestClientResponseException e) {
            throw new RestConnectorException(
                    "HTTP " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            throw new RestConnectorException("REST_UNAVAILABLE");
        }
    }
}
