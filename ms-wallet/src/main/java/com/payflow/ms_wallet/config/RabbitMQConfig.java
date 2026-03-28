package com.payflow.ms_wallet.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // O nome da nossa Central de Distribuição
    public static final String TRANSFER_EXCHANGE = "transfer.exchange";

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(TRANSFER_EXCHANGE); // Cria a central no RabbitMQ
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}