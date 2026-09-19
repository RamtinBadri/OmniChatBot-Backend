package dev.ramtin.aiplatform.exception;

public class AIProviderException extends RuntimeException {
    public AIProviderException(String message) {
        super(message);
    }

    public AIProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}