package com.payflow.ms_ai_categorizer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;

@Service
public class AICategorizerService {

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.base-url}")
    private String baseUrl;

    @Value("${openrouter.model}")
    private String model;

    public String categorize(String description) {
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

            List choices = (List) response.get("choices");
            Map firstChoice = (Map) choices.get(0);
            Map message = (Map) firstChoice.get("message");
            String content = (String) message.get("content");

            return content.trim().toUpperCase().replaceAll("[^A-Z]", "");

        } catch (Exception e) {
            System.err.println("ERRO AO CHAMAR OPENROUTER: " + e.getMessage());
            return "NÃO CATEGORIZADO";
        }
    }
}