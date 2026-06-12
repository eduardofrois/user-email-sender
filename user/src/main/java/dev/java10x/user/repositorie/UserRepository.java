package dev.java10x.user.repositorie;
import dev.java10x.user.domain.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel, UUID> {
}
