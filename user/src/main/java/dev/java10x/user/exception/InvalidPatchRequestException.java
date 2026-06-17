package dev.java10x.user.exception;

public class InvalidPatchRequestException extends RuntimeException {

    public InvalidPatchRequestException(String message) {
        super(message);
    }
}
