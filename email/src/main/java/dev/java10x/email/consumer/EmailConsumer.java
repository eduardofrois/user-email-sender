package dev.java10x.email.consumer;
import dev.java10x.email.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    private final EmailService emailService;

    public EmailConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "email-queue")
    public void listenEmailQueue(@Payload String text) {
        System.out.println(text);
    }

    @RabbitListener(queues = "users-list-queue")
    public void listenUsersListQueue(@Payload String text) {
        System.out.println(text);
    }

    @RabbitListener(queues = "simulated-delay-queue")
    public void listenSimulatedDelayQueue(@Payload String text) {
        emailService.simulateEmailSending(text);
    }

}
