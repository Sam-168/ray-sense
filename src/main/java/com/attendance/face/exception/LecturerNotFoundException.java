package com.attendance.face.exception;

public class LecturerNotFoundException extends RuntimeException{
    public LecturerNotFoundException(String message) {
        super(message);
    }

    public LecturerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
