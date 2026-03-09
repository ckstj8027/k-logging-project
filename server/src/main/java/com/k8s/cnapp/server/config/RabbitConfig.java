package com.k8s.cnapp.server.config;

import com.k8s.cnapp.server.detection.event.RabbitSecurityEventPublisher;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue scanQueue() {
        return new Queue(RabbitSecurityEventPublisher.SCAN_QUEUE, true);
    }

    @Bean
    public org.springframework.amqp.support.converter.MessageConverter jackson2JsonMessageConverter() {
        return new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter();
    }
}
