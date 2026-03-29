package com.payflow.ms_ai_categorizer.consumer;

import com.payflow.ms_ai_categorizer.dto.CategoryResultEvent;
import com.payflow.ms_ai_categorizer.dto.TransferEvent;
import com.payflow.ms_ai_categorizer.service.AICategorizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AIConsumer {

    private static final Logger log = LoggerFactory.getLogger(AIConsumer.class);
    private static final String FALLBACK_CATEGORY = "OUTROS";

    @Autowired
    private AICategorizerService aiService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @org.springframework.amqp.rabbit.annotation.QueueBinding(
            value = @org.springframework.amqp.rabbit.annotation.Queue(value = "queue.ai", durable = "true"),
            exchange = @org.springframework.amqp.rabbit.annotation.Exchange(value = "transfer.exchange", type = "fanout")
    ))
    public void processAIClassification(TransferEvent event) {
        if (event == null || event.transactionId() == null || event.description() == null || event.description().isBlank()) {
            log.warn("Evento invalido recebido pela IA; mensagem ignorada");
            return;
        }

        log.info("IA analisando transacao. txId={}", event.transactionId());

        String category;
        try {
            category = aiService.categorize(event.description());
        } catch (Exception exception) {
            log.error("Falha ao classificar transacao na IA. txId={}", event.transactionId(), exception);
            category = FALLBACK_CATEGORY;
        }

        CategoryResultEvent resultEvent = new CategoryResultEvent(event.transactionId(), category);
        rabbitTemplate.convertAndSend("transfer.category.result", resultEvent);

        log.info("Resultado de categorizacao enviado. txId={}, category={}", event.transactionId(), category);
    }
}