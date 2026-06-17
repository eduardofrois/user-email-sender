package dev.java10x.user.service;

import dev.java10x.user.domain.UserModel;
import dev.java10x.user.dto.CreateUserRequest;
import dev.java10x.user.dto.PatchUserRequest;
import dev.java10x.user.dto.UpdateUserRequest;
import dev.java10x.user.dto.UserResponse;
import dev.java10x.user.exception.InvalidPatchRequestException;
import dev.java10x.user.exception.UserNotFoundException;
import dev.java10x.user.mapper.UserMapper;
import dev.java10x.user.producer.UserProducer;
import dev.java10x.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserProducer userProducer;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserProducer userProducer, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userProducer = userProducer;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        UserModel user = userMapper.toModel(request);
        UserModel createdUser = userRepository.save(user);
        userProducer.publishUserCreatedEvent(createdUser);
        return userMapper.toResponse(createdUser);
    }

    @Transactional
    public List<UserResponse> createUsers(List<CreateUserRequest> requests) {
        List<UserModel> users = requests.stream()
                .map(userMapper::toModel)
                .toList();

        List<UserModel> createdUsers = userRepository.saveAll(users);
        userProducer.publishSimulatedDelayRequestedEvents(createdUsers);
        return userMapper.toResponseList(createdUsers);
    }

    public List<UserResponse> findAllUsers() {
        List<UserModel> users = userRepository.findAll();
        return userMapper.toResponseList(users);
    }

    public UserResponse findUserById(UUID userId) {
        return userMapper.toResponse(getUserOrThrow(userId));
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        UserModel user = getUserOrThrow(userId);
        userMapper.updateModel(user, request);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse patchUser(UUID userId, PatchUserRequest request) {
        validatePatchRequest(request);

        UserModel user = getUserOrThrow(userId);
        userMapper.patchModel(user, request);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID userId) {
        UserModel user = getUserOrThrow(userId);
        userRepository.delete(user);
    }

    private UserModel getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void validatePatchRequest(PatchUserRequest request) {
        boolean hasName = request.name() != null;
        boolean hasEmail = request.email() != null;

        if (!hasName && !hasEmail) {
            throw new InvalidPatchRequestException("Informe ao menos um campo para atualizar");
        }

        if (hasName && request.name().isBlank()) {
            throw new InvalidPatchRequestException("Nome não pode ser vazio");
        }

        if (hasEmail && request.email().isBlank()) {
            throw new InvalidPatchRequestException("Email não pode ser vazio");
        }
    }
}
