package com.payflow.ms_ai_categorizer.config;

import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        // Criamos o mapeador de tipos
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();

        // Esta é a LINHA MÁGICA:
        // Dizemos ao Spring para confiar em todos os pacotes ("*")
        typeMapper.setTrustedPackages("*");

        // Atribuímos o mapeador ao conversor usando o método correto
        converter.setJavaTypeMapper(typeMapper);

        return converter;
    }
}