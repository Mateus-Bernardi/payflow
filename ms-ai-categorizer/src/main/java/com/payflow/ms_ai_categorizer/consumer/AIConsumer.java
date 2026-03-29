package com.payflow.ms_ai_categorizer.consumer;

import com.payflow.ms_ai_categorizer.dto.CategoryResultEvent;
import com.payflow.ms_ai_categorizer.dto.TransferEvent;
import com.payflow.ms_ai_categorizer.service.AICategorizerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AIConsumer {

    @Autowired
    private AICategorizerService aiService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @org.springframework.amqp.rabbit.annotation.QueueBinding(
            value = @org.springframework.amqp.rabbit.annotation.Queue(value = "queue.ai", durable = "true"),
            exchange = @org.springframework.amqp.rabbit.annotation.Exchange(value = "transfer.exchange", type = "fanout")
    ))
    public void processAIClassification(TransferEvent event) {
        System.out.println("IA Analisando transação: " + event.description());

        String category = aiService.categorize(event.description());
        System.out.println("Categoria Identificada: " + category);

        CategoryResultEvent resultEvent = new CategoryResultEvent(event.transactionId(), category);

        rabbitTemplate.convertAndSend("transfer.category.result", resultEvent);
        System.out.println("esposta enviada de volta para o Wallet!");
        System.out.println("--------------------------------------------------");
    }
}