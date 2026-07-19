package fr.webskills.academy.exception;

public class ExpiredAccessCodeException extends RuntimeException {
    public ExpiredAccessCodeException(String message) {
        super(message);
    }
}
