package dev.java10x.user.controller;

import dev.java10x.user.domain.UserModel;
import dev.java10x.user.dto.UserDto;
import dev.java10x.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users", description = "Operations related to users")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create a new user", description = "Creates a user and publishes the event for sending email.")
    @PostMapping("/create")
    public ResponseEntity<UserModel> createUser(@RequestBody UserDto userDto) {
        var userModel = new UserModel();
        BeanUtils.copyProperties(userDto, userModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveAndPublish(userModel));
    }

    @Operation(summary = "Create users in batch", description = "Creates a list of users.")
    @PostMapping("/create/batch")
    public ResponseEntity<List<UserModel>> createUsers(@RequestBody List<UserDto> userDtos) {
        List<UserModel> users = userDtos.stream()
                .map(userDto -> {
                    var userModel = new UserModel();
                    BeanUtils.copyProperties(userDto, userModel);
                    return userModel;
                })
                .toList();
        
        
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveAll(users));
    }

    @Operation(summary = "List all users", description = "Returns a list of all registered users.")
    @GetMapping("/list")
    public ResponseEntity<List<UserModel>> getAllUsers() {
        List<UserModel> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
