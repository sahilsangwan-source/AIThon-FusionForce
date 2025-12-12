package com.lms.trainingservice.exception;

/**
 * Exception thrown when a training resource is not found
 */
public class TrainingNotFoundException extends RuntimeException {

    public TrainingNotFoundException(String message) {
        super(message);
    }

    public TrainingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
