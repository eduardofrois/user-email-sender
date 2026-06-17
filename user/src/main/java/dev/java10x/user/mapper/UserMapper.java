package dev.java10x.user.mapper;

import dev.java10x.user.domain.UserModel;
import dev.java10x.user.dto.CreateUserRequest;
import dev.java10x.user.dto.PatchUserRequest;
import dev.java10x.user.dto.UpdateUserRequest;
import dev.java10x.user.dto.UserResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public UserModel toModel(CreateUserRequest request) {
        var user = new UserModel();
        user.setName(request.name());
        user.setEmail(request.email());
        return user;
    }

    public UserResponse toResponse(UserModel user) {
        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail()
        );
    }

    public List<UserResponse> toResponseList(List<UserModel> users) {
        return users.stream()
                .map(this::toResponse)
                .toList();
    }

    public void updateModel(UserModel user, UpdateUserRequest request) {
        user.setName(request.name());
        user.setEmail(request.email());
    }

    public void patchModel(UserModel user, PatchUserRequest request) {
        if (request.name() != null) {
            user.setName(request.name());
        }

        if (request.email() != null) {
            user.setEmail(request.email());
        }
    }
}
