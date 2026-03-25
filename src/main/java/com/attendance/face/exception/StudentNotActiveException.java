package com.attendance.face.exception;

public class StudentNotActiveException extends RuntimeException{

    public StudentNotActiveException(String message){
        super(message);
    }
    public StudentNotActiveException(String message,Throwable cause){
        super(message, cause);
    }
}
