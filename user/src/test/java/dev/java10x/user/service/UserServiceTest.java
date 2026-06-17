package dev.java10x.user.service;

import dev.java10x.user.domain.UserModel;
import dev.java10x.user.dto.CreateUserRequest;
import dev.java10x.user.dto.PatchUserRequest;
import dev.java10x.user.exception.InvalidPatchRequestException;
import dev.java10x.user.exception.UserNotFoundException;
import dev.java10x.user.mapper.UserMapper;
import dev.java10x.user.producer.UserProducer;
import dev.java10x.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProducer userProducer;

    private final UserMapper userMapper = new UserMapper();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userProducer, userMapper);
    }

    @Test
    void createUserSavesAndPublishesEmailEvent() {
        UUID userId = UUID.randomUUID();

        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> {
            UserModel user = invocation.getArgument(0);
            user.setUserId(userId);
            return user;
        });

        var response = userService.createUser(new CreateUserRequest("Alice", "alice@email.com"));

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.email()).isEqualTo("alice@email.com");

        ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);
        verify(userProducer).publishUserCreatedEvent(userCaptor.capture());
        assertThat(userCaptor.getValue().getUserId()).isEqualTo(userId);
    }

    @Test
    void findAllUsersDoesNotPublishEvents() {
        when(userRepository.findAll()).thenReturn(List.of(user("Alice", "alice@email.com")));

        var response = userService.findAllUsers();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).name()).isEqualTo("Alice");
        verifyNoInteractions(userProducer);
    }

    @Test
    void findUserByIdThrowsWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    @Test
    void patchUserRejectsEmptyRequest() {
        assertThatThrownBy(() -> userService.patchUser(UUID.randomUUID(), new PatchUserRequest(null, null)))
                .isInstanceOf(InvalidPatchRequestException.class)
                .hasMessage("Informe ao menos um campo para atualizar");
    }

    private UserModel user(String name, String email) {
        var user = new UserModel();
        user.setUserId(UUID.randomUUID());
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
