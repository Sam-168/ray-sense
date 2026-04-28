package com.attendance.face.exception;

public class FaceNotRecognizedException extends RuntimeException{

    public FaceNotRecognizedException(String message) {
        super(message);
    }

    public FaceNotRecognizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
