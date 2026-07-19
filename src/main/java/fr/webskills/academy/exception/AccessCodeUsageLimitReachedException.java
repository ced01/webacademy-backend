package fr.webskills.academy.exception;

public class AccessCodeUsageLimitReachedException extends RuntimeException {
    public AccessCodeUsageLimitReachedException(String message) {
        super(message);
    }
}
