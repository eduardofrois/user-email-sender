package dev.java10x.email.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue emailNotificationQueue(
            @Value("${app.rabbitmq.queues.email-notification}") String queueName
    ) {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue simulatedDelayQueue(
            @Value("${app.rabbitmq.queues.simulated-delay}") String queueName
    ) {
        return new Queue(queueName, true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
