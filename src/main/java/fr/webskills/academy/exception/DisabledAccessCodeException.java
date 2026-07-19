package fr.webskills.academy.exception;

public class DisabledAccessCodeException extends RuntimeException {
    public DisabledAccessCodeException(String message) {
        super(message);
    }
}
