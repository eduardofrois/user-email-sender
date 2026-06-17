package dev.java10x.email.service;

import dev.java10x.email.domain.EmailModel;
import dev.java10x.email.dto.UserEventDto;
import dev.java10x.email.enums.EmailStatus;
import dev.java10x.email.helpers.Delay;
import dev.java10x.email.mapper.EmailMapper;
import dev.java10x.email.repositorie.EmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final EmailRepository emailRepository;
    private final EmailMapper emailMapper;
    private final Delay delay;
    private final String emailFrom;

    public EmailService(
            JavaMailSender mailSender,
            EmailRepository emailRepository,
            EmailMapper emailMapper,
            Delay delay,
            @Value("${spring.mail.username}") String emailFrom
    ) {
        this.mailSender = mailSender;
        this.emailRepository = emailRepository;
        this.emailMapper = emailMapper;
        this.delay = delay;
        this.emailFrom = emailFrom;
    }

    @Transactional
    public void sendUserCreatedEmail(UserEventDto event) {
        EmailModel emailModel = emailMapper.toUserCreatedEmail(event, emailFrom);
        sendEmail(emailModel);
    }

    @Transactional
    public void sendEmail(EmailModel emailModel) {
        initializeHistory(emailModel);
        emailRepository.save(emailModel);

        try {
            registerAttempt(emailModel);
            mailSender.send(buildMessage(emailModel));
            markAsSent(emailModel);
        } catch (MailException exception) {
            markAsFailed(emailModel, exception);
            log.error("Failed to send email to user {}", emailModel.getUserId(), exception);
        }

        emailRepository.save(emailModel);
    }

    public void simulateEmailSending(UserEventDto event) {
        log.info("Simulated email sent to user {} at {}", event.userId(), event.email());
        delay.simulateDelay();
    }

    private SimpleMailMessage buildMessage(EmailModel emailModel) {
        var message = new SimpleMailMessage();
        message.setFrom(emailModel.getEmailFrom());
        message.setTo(emailModel.getEmailTo());
        message.setSubject(emailModel.getEmailSubject());
        message.setText(emailModel.getBody());
        return message;
    }

    private void initializeHistory(EmailModel emailModel) {
        if (emailModel.getCreatedAt() == null) {
            emailModel.setCreatedAt(LocalDateTime.now());
        }

        if (emailModel.getAttempts() == null) {
            emailModel.setAttempts(0);
        }

        if (emailModel.getStatusEmail() == null) {
            emailModel.setStatusEmail(EmailStatus.PENDING);
        }
    }

    private void registerAttempt(EmailModel emailModel) {
        emailModel.setAttempts(emailModel.getAttempts() + 1);
        emailModel.setLastAttemptAt(LocalDateTime.now());
        emailModel.setErrorMessage(null);
    }

    private void markAsSent(EmailModel emailModel) {
        emailModel.setStatusEmail(EmailStatus.SENT);
        emailModel.setSendDateEmail(LocalDateTime.now());
    }

    private void markAsFailed(EmailModel emailModel, MailException exception) {
        emailModel.setStatusEmail(EmailStatus.FAILED);
        emailModel.setErrorMessage(exception.getMessage());
    }
}
