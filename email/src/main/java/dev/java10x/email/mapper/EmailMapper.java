package dev.java10x.email.mapper;

import dev.java10x.email.domain.EmailModel;
import dev.java10x.email.dto.EmailHistoryResponse;
import dev.java10x.email.dto.UserEventDto;
import dev.java10x.email.enums.EmailStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailMapper {

    private static final String USER_CREATED_SUBJECT = "Cadastro realizado com sucesso";

    public EmailModel toUserCreatedEmail(UserEventDto event, String emailFrom) {
        var emailModel = new EmailModel();
        emailModel.setUserId(event.userId());
        emailModel.setEmailFrom(emailFrom);
        emailModel.setEmailTo(event.email());
        emailModel.setEmailSubject(USER_CREATED_SUBJECT);
        emailModel.setBody(buildUserCreatedBody(event));
        emailModel.setOriginEventType(event.eventType());
        emailModel.setStatusEmail(EmailStatus.PENDING);
        return emailModel;
    }

    public EmailHistoryResponse toHistoryResponse(EmailModel emailModel) {
        return new EmailHistoryResponse(
                emailModel.getEmailId(),
                emailModel.getUserId(),
                emailModel.getEmailFrom(),
                emailModel.getEmailTo(),
                emailModel.getEmailSubject(),
                emailModel.getBody(),
                emailModel.getOriginEventType(),
                emailModel.getStatusEmail(),
                emailModel.getAttempts(),
                emailModel.getCreatedAt(),
                emailModel.getLastAttemptAt(),
                emailModel.getSendDateEmail(),
                emailModel.getErrorMessage()
        );
    }

    public List<EmailHistoryResponse> toHistoryResponseList(List<EmailModel> emails) {
        return emails.stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private String buildUserCreatedBody(UserEventDto event) {
        return "Olá, " + event.name() + "! Seu cadastro foi realizado com sucesso.";
    }
}
