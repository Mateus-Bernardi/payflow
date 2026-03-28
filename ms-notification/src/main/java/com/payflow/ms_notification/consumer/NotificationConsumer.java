package com.payflow.ms_notification.consumer;

import com.payflow.ms_notification.dto.TransferEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    @RabbitListener(bindings = @org.springframework.amqp.rabbit.annotation.QueueBinding(
            value = @org.springframework.amqp.rabbit.annotation.Queue(value = "queue.notification", durable = "true"),
            exchange = @org.springframework.amqp.rabbit.annotation.Exchange(value = "transfer.exchange", type = "fanout")
    ))
    public void listenTransferNotification(TransferEvent event) {

        System.out.println("--------------------------------------------------");
        System.out.println("NOTIFICAÇÃO RECEBIDA COM SUCESSO!");
        System.out.println("Remetente ID: " + event.senderId());
        System.out.println("Enviando e-mail para o Recebedor: " + event.receiverId());
        System.out.println("Valor: R$ " + event.value());

        this.simulateEmailSending();

        System.out.println("E-mail enviado com sucesso!");
        System.out.println("--------------------------------------------------");
    }

    private void simulateEmailSending() {
        try {
            Thread.sleep(3000); // Simula o atraso de 3 segundos para enviar um e-mail
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}