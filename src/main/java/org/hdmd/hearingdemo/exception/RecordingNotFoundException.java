package org.hdmd.hearingdemo.exception;

import org.springframework.http.HttpStatus;

public class RecordingNotFoundException extends RuntimeException {

    public RecordingNotFoundException(String message) {
        super(message);
    }

    public RecordingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecordingNotFoundException(HttpStatus httpStatus, String s) {
        super(httpStatus.name());
    }
}
