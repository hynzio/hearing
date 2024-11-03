package org.hdmd.hearingdemo.exception;

public class HistoryNotFoundException extends RuntimeException {

    public HistoryNotFoundException(String message) {
        super(message);
    }

    public HistoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
