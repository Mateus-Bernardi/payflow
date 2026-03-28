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

        // 1. Configurar os Headers (Cabeçalhos) obrigatórios do OpenRouter
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("HTTP-Referer", "http://localhost:8084"); // Exigido pelo OpenRouter
        headers.set("X-Title", "PayFlowPro"); // Exigido pelo OpenRouter

        // 2. Montar o Prompt
        String prompt = "Responda APENAS com uma única palavra que represente a categoria da transação: '" + description + "'. " +
                "Opções: ALIMENTAÇÃO, TRANSPORTE, LAZER, SAÚDE, EDUCAÇÃO, OUTROS.";

        // 3. Montar o Corpo da Requisição (Padrão OpenAI/OpenRouter)
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        // Criar a entidade que junta Headers + Body
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 4. Fazer a chamada POST
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

            // 5. Navegar na resposta (Estrutura: choices[0].message.content)
            List choices = (List) response.get("choices");
            Map firstChoice = (Map) choices.get(0);
            Map message = (Map) firstChoice.get("message");
            String content = (String) message.get("content");

            return content.trim().toUpperCase().replaceAll("[^A-Z]", "");

        } catch (Exception e) {
            System.err.println("❌ ERRO AO CHAMAR OPENROUTER: " + e.getMessage());
            return "NÃO CATEGORIZADO";
        }
    }
}