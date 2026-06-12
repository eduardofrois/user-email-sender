package dev.java10x.email.repositorie;
import dev.java10x.email.domain.EmailModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EmailRepository extends JpaRepository<EmailModel, UUID> {
}
