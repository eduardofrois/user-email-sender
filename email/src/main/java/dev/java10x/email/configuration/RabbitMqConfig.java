package dev.java10x.email.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.java10x.email.dto.UserEventDto;
import dev.java10x.email.service.EmailService;
import org.aopalliance.aop.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqConfig.class);

    @Bean
    public Queue emailNotificationQueue(
            @Value("${app.rabbitmq.queues.email-notification}") String queueName,
            @Value("${app.rabbitmq.exchanges.email-dead-letter}") String deadLetterExchange,
            @Value("${app.rabbitmq.queues.email-dead-letter}") String deadLetterQueue
    ) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", deadLetterQueue)
                .build();
    }

    @Bean
    public Queue simulatedDelayQueue(
            @Value("${app.rabbitmq.queues.simulated-delay}") String queueName
    ) {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue emailDeadLetterQueue(
            @Value("${app.rabbitmq.queues.email-dead-letter}") String queueName
    ) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public DirectExchange emailDeadLetterExchange(
            @Value("${app.rabbitmq.exchanges.email-dead-letter}") String exchangeName
    ) {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding emailDeadLetterBinding(
            Queue emailDeadLetterQueue,
            DirectExchange emailDeadLetterExchange,
            @Value("${app.rabbitmq.queues.email-dead-letter}") String routingKey
    ) {
        return BindingBuilder.bind(emailDeadLetterQueue)
                .to(emailDeadLetterExchange)
                .with(routingKey);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory emailRetryListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            Advice emailRetryInterceptor
    ) {
        var factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setAdviceChain(emailRetryInterceptor);
        return factory;
    }

    @Bean
    public Advice emailRetryInterceptor(
            MessageRecoverer emailMessageRecoverer,
            @Value("${app.rabbitmq.retry.max-attempts}") int maxAttempts,
            @Value("${app.rabbitmq.retry.initial-interval}") long initialInterval,
            @Value("${app.rabbitmq.retry.multiplier}") double multiplier,
            @Value("${app.rabbitmq.retry.max-interval}") long maxInterval
    ) {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(maxAttempts)
                .backOffOptions(initialInterval, multiplier, maxInterval)
                .recoverer(emailMessageRecoverer)
                .build();
    }

    @Bean
    public MessageRecoverer emailMessageRecoverer(
            EmailService emailService,
            ObjectMapper objectMapper
    ) {
        return (message, cause) -> {
            try {
                var event = objectMapper.readValue(message.getBody(), UserEventDto.class);
                emailService.markAsFailedAfterRetries(event, cause);
            } catch (Exception exception) {
                log.error("Failed to update email history before sending message to DLQ", exception);
            }

            throw new AmqpRejectAndDontRequeueException("Email retries exhausted", cause);
        };
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
