package dev.java10x.email.consumer;
import dev.java10x.email.domain.EmailModel;
import dev.java10x.email.dto.EmailDto;
import dev.java10x.email.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

//    final EmailService emailService;
//
//    public EmailConsumer(EmailService emailService) {
//        this.emailService = emailService;
//    }

    @RabbitListener(queues = "email-queue")
    public void listenEmailQueue(@Payload String text) {

//       var emailModel = new EmailModel();
//        BeanUtils.copyProperties(emailDto, emailModel);
//        emailService.sendEmail(emailModel);

        System.out.println(text);
    }

}
