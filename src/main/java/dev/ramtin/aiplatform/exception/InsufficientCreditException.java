package dev.ramtin.aiplatform.exception;

public class InsufficientCreditException extends RuntimeException {
    public InsufficientCreditException(String message) {
      super(message);
    }
}