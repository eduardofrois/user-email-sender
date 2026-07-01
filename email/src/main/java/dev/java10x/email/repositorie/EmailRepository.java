package dev.java10x.email.repositorie;

import dev.java10x.email.domain.EmailModel;
import dev.java10x.email.enums.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailRepository extends JpaRepository<EmailModel, UUID> {

    List<EmailModel> findByUserId(UUID userId);

    List<EmailModel> findByStatusEmail(EmailStatus statusEmail);

    Optional<EmailModel> findFirstByUserIdAndOriginEventTypeAndStatusEmailOrderByCreatedAtDesc(
            UUID userId,
            String originEventType,
            EmailStatus statusEmail
    );
}
