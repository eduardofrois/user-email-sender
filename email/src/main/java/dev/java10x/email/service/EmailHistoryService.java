package dev.java10x.email.service;

import dev.java10x.email.dto.EmailHistoryResponse;
import dev.java10x.email.enums.EmailStatus;
import dev.java10x.email.exception.EmailNotFoundException;
import dev.java10x.email.mapper.EmailMapper;
import dev.java10x.email.repositorie.EmailRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EmailHistoryService {

    private final EmailRepository emailRepository;
    private final EmailMapper emailMapper;

    public EmailHistoryService(EmailRepository emailRepository, EmailMapper emailMapper) {
        this.emailRepository = emailRepository;
        this.emailMapper = emailMapper;
    }

    public List<EmailHistoryResponse> findAll() {
        return emailMapper.toHistoryResponseList(emailRepository.findAll());
    }

    public EmailHistoryResponse findById(UUID emailId) {
        return emailRepository.findById(emailId)
                .map(emailMapper::toHistoryResponse)
                .orElseThrow(() -> new EmailNotFoundException(emailId));
    }

    public List<EmailHistoryResponse> findByUserId(UUID userId) {
        return emailMapper.toHistoryResponseList(emailRepository.findByUserId(userId));
    }

    public List<EmailHistoryResponse> findByStatus(EmailStatus status) {
        return emailMapper.toHistoryResponseList(emailRepository.findByStatusEmail(status));
    }
}
