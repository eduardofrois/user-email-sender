package dev.java10x.user.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super("Usuário não encontrado para o ID informado: " + userId);
    }
}
