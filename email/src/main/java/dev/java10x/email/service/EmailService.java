package dev.java10x.email.service;
import dev.java10x.email.domain.EmailModel;
import dev.java10x.email.enums.EmailStatus;
import dev.java10x.email.repositorie.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailRepository emailRepository;

    @Value("${EMAIL_USERNAME}")
    private String emailFrom;

    @Transactional
    public void sendEmail(EmailModel emailModel) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(emailModel.getEmailTo());
            message.setSubject(emailModel.getEmailSubject());
            message.setText(emailModel.getBody());
            mailSender.send(message);
            emailModel.setStatusEmail(EmailStatus.SENT);
            emailModel.setSendDateEmail(LocalDateTime.now());
        } catch (Exception e) {
            emailModel.setStatusEmail(EmailStatus.FAILED);
            System.out.println("Erro ao enviar email: " + e.getMessage());
        }

        emailRepository.save(emailModel);
    }

}
