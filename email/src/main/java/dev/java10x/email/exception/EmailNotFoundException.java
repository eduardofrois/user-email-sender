package dev.java10x.email.exception;

import java.util.UUID;

public class EmailNotFoundException extends RuntimeException {

    public EmailNotFoundException(UUID emailId) {
        super("Histórico de e-mail não encontrado para o ID informado: " + emailId);
    }
}
