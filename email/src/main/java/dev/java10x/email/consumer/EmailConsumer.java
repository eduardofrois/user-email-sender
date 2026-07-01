package dev.java10x.email.consumer;

import dev.java10x.email.dto.UserEventDto;
import dev.java10x.email.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);

    private final EmailService emailService;

    public EmailConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(
            queues = "${app.rabbitmq.queues.email-notification}",
            containerFactory = "emailRetryListenerContainerFactory"
    )
    public void listenEmailNotificationQueue(@Payload UserEventDto event) {
        log.info("Received user created event for user {}", event.userId());
        emailService.sendUserCreatedEmail(event);
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.simulated-delay}")
    public void listenSimulatedDelayQueue(@Payload UserEventDto event) {
        emailService.simulateEmailSending(event);
    }

}
