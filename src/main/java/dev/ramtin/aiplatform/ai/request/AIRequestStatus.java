package dev.ramtin.aiplatform.ai.request;

public enum AIRequestStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    TIMEOUT,
    RATE_LIMITED,
    PROVIDER_ERROR
}