package com.payflow.ms_notification.consumer;

import com.payflow.ms_notification.dto.TransferEvent;
import com.payflow.ms_notification.exception.InvalidTransferEventException;
import com.payflow.ms_notification.exception.NotificationProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @RabbitListener(bindings = @org.springframework.amqp.rabbit.annotation.QueueBinding(
            value = @org.springframework.amqp.rabbit.annotation.Queue(value = "queue.notification", durable = "true"),
            exchange = @org.springframework.amqp.rabbit.annotation.Exchange(value = "transfer.exchange", type = "fanout")
    ))
    public void listenTransferNotification(TransferEvent event) {
        try {
            validateEvent(event);

            log.info("Notificacao recebida. txId={}, senderId={}, receiverId={}, value={}",
                    event.transactionId(), event.senderId(), event.receiverId(), event.value());

            simulateEmailSending();

            log.info("Notificacao processada com sucesso. txId={}", event.transactionId());
        } catch (InvalidTransferEventException exception) {
            // Evento malformado nao deve derrubar o consumidor.
            log.warn("Evento de transferencia invalido ignorado: {}", exception.getMessage());
        } catch (Exception exception) {
            log.error("Falha ao processar notificacao de transferencia", exception);
            throw new NotificationProcessingException("Erro ao processar notificacao", exception);
        }
    }

    private void validateEvent(TransferEvent event) {
        if (event == null) {
            throw new InvalidTransferEventException("Evento nulo");
        }

        if (event.transactionId() == null || event.senderId() == null || event.receiverId() == null || event.value() == null) {
            throw new InvalidTransferEventException("Campos obrigatorios ausentes no evento");
        }

        if (Objects.equals(event.senderId(), event.receiverId())) {
            throw new InvalidTransferEventException("Remetente e destinatario nao podem ser iguais");
        }
    }

    private void simulateEmailSending() {
        try {
            Thread.sleep(3000); // Simula o atraso de 3 segundos para enviar um e-mail
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NotificationProcessingException("Thread interrompida durante o envio de notificacao", e);
        }
    }
}