package dev.ramtin.aiplatform.exception;

public class ModelPricingNotFoundException extends RuntimeException {
    public ModelPricingNotFoundException(String message) {
        super(message);
    }
}