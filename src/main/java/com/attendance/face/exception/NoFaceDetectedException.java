package com.attendance.face.exception;

public class NoFaceDetectedException extends RuntimeException{

        public NoFaceDetectedException(String message) {
            super(message);
        }

        public NoFaceDetectedException(String message, Throwable cause) {
            super(message, cause);

    }
}
