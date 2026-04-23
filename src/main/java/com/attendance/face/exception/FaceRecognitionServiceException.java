package com.attendance.face.exception;

public class FaceRecognitionServiceException extends RuntimeException{
    public FaceRecognitionServiceException(String message) {
        super(message);
    }

    public FaceRecognitionServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
