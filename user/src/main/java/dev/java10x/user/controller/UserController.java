package dev.java10x.user.controller;

import dev.java10x.user.dto.CreateUserRequest;
import dev.java10x.user.dto.PatchUserRequest;
import dev.java10x.user.dto.UpdateUserRequest;
import dev.java10x.user.dto.UserResponse;
import dev.java10x.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Users", description = "Operations related to users")
@Validated
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create a new user", description = "Creates a user and publishes the event for sending email.")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(summary = "Create users in batch", description = "Creates users and publishes simulated delay events.")
    @PostMapping("/batch")
    public ResponseEntity<List<UserResponse>> createUsers(
            @RequestBody @Valid List<@Valid CreateUserRequest> requests
    ) {
        List<UserResponse> users = userService.createUsers(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(users);
    }

    @Operation(summary = "List all users", description = "Returns a list of all registered users.")
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @Operation(summary = "Get user by code", description = "Returns a user by UUID code.")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> findUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.findUserById(userId));
    }

    @Operation(summary = "Update a user", description = "Replaces the editable user fields.")
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @RequestBody @Valid UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @Operation(summary = "Patch a user", description = "Updates only the provided user fields.")
    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponse> patchUser(
            @PathVariable UUID userId,
            @RequestBody @Valid PatchUserRequest request
    ) {
        return ResponseEntity.ok(userService.patchUser(userId, request));
    }

    @Operation(summary = "Delete a user", description = "Deletes a user by UUID code.")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
