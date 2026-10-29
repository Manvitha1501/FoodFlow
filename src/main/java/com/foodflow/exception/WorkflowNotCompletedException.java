package com.foodflow.exception;

public class WorkflowNotCompletedException extends RuntimeException {
    public WorkflowNotCompletedException(String message) {
        super(message);
    }
}
