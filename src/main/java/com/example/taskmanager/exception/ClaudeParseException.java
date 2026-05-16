package com.example.taskmanager.exception;

/**
 * Thrown when Claude returns a response that cannot be parsed into a valid TaskSuggestion.
 * Signals an upstream failure (502 Bad Gateway), not a bug in our own code.
 */
public class ClaudeParseException extends RuntimeException {

    public ClaudeParseException(String message) {
        super(message);
    }

    public ClaudeParseException(String message, Throwable cause) {
        super(message, cause);
    }
}