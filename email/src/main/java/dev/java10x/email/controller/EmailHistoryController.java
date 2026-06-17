package dev.java10x.email.controller;

import dev.java10x.email.dto.EmailHistoryResponse;
import dev.java10x.email.enums.EmailStatus;
import dev.java10x.email.service.EmailHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emails")
public class EmailHistoryController {

    private final EmailHistoryService emailHistoryService;

    public EmailHistoryController(EmailHistoryService emailHistoryService) {
        this.emailHistoryService = emailHistoryService;
    }

    @GetMapping
    public ResponseEntity<List<EmailHistoryResponse>> findAll() {
        return ResponseEntity.ok(emailHistoryService.findAll());
    }

    @GetMapping("/{emailId}")
    public ResponseEntity<EmailHistoryResponse> findById(@PathVariable UUID emailId) {
        return ResponseEntity.ok(emailHistoryService.findById(emailId));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<EmailHistoryResponse>> findByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(emailHistoryService.findByUserId(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmailHistoryResponse>> findByStatus(@PathVariable EmailStatus status) {
        return ResponseEntity.ok(emailHistoryService.findByStatus(status));
    }
}
