package com.lms.trainingservice.exception;

/**
 * Global exception handler for Training Service
 */
public class TrainingException extends RuntimeException {

    public TrainingException(String message) {
        super(message);
    }

    public TrainingException(String message, Throwable cause) {
        super(message, cause);
    }

}
