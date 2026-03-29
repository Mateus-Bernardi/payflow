package com.payflow.ms_ai_categorizer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;

@Service
public class AICategorizerService {

    private static final Logger log = LoggerFactory.getLogger(AICategorizerService.class);
    private static final String FALLBACK_CATEGORY = "OUTROS";

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.base-url}")
    private String baseUrl;

    @Value("${openrouter.model}")
    private String model;

    public String categorize(String description) {
        if (description == null || description.isBlank()) {
            return FALLBACK_CATEGORY;
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("HTTP-Referer", "http://localhost:8084");
        headers.set("X-Title", "PayFlowPro");

        String prompt = "Responda APENAS com uma única palavra que represente a categoria da transação: '" + description + "'. " +
                "Opções: ALIMENTAÇÃO, TRANSPORTE, LAZER, SAÚDE, EDUCAÇÃO, OUTROS.";

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            String content = extractContent(response);

            if (content == null || content.isBlank()) {
                return FALLBACK_CATEGORY;
            }

            return content.trim().toUpperCase().replaceAll("[^A-Z]", "");
        } catch (RestClientException exception) {
            log.error("Erro ao chamar provedor de IA", exception);
            return FALLBACK_CATEGORY;
        } catch (Exception exception) {
            log.error("Resposta inesperada do provedor de IA", exception);
            return FALLBACK_CATEGORY;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        if (response == null || !(response.get("choices") instanceof List<?> choices) || choices.isEmpty()) {
            return null;
        }

        Object firstChoiceObj = choices.get(0);
        if (!(firstChoiceObj instanceof Map<?, ?> firstChoice)) {
            return null;
        }

        Object messageObj = firstChoice.get("message");
        if (!(messageObj instanceof Map<?, ?> message)) {
            return null;
        }

        Object contentObj = message.get("content");
        if (!(contentObj instanceof String content)) {
            return null;
        }

        return content;
    }
}