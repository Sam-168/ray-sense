package com.attendance.face.exception;
import com.attendance.face.dto.response.ErrorResponse;
import com.attendance.face.dto.response.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStudentNotFound(
            StudentNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }


    @ExceptionHandler(DuplicateStudentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateStudent(
            DuplicateStudentException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DuplicateAttendanceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateAttendance(
            DuplicateAttendanceException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }


    @ExceptionHandler(StudentNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleStudentNotActive(
            StudentNotActiveException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        ValidationErrorResponse error = new ValidationErrorResponse(
                "Validation failed",
                request.getRequestURI()
        );

        // Extract field errors
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            error.addFieldError(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    @ExceptionHandler(FaceRecognitionServiceException.class)
    public ResponseEntity<ErrorResponse> handleFaceRecognitionService(
            FaceRecognitionServiceException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }


    /**
     * Handle invalid login credentials
     * Returns 401 Unauthorized
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage(), request.getRequestURI()));
    }

    /**
     * Handle expired JWT tokens
     * Returns 401 Unauthorized
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(
            TokenExpiredException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage(), request.getRequestURI()));
    }

    /**
     * Handle unauthorized access attempts
     * Returns 403 Forbidden
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getMessage(), request.getRequestURI()));
    }

    /**
     * Handle Spring Security access denied
     * Returns 403 Forbidden
     * Thrown when user is logged in but doesn't have required role
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(
                        "You don't have permission to access this resource",
                        request.getRequestURI()
                ));
    }

    /**
     * Handle user not found
     * Returns 404 Not Found
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), request.getRequestURI()));
    }

    /**
     * Handle lecturer not found
     * Returns 404 Not Found
     */
    @ExceptionHandler(LecturerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLecturerNotFound(
            LecturerNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), request.getRequestURI()));
    }

    /**
     * Handle duplicate email registration
     * Returns 409 Conflict
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), request.getRequestURI()));
    }

    /**
     * Handle face registration failure
     * Returns 422 Unprocessable Entity
     */
    @ExceptionHandler(FaceRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleFaceRegistration(
            FaceRegistrationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(FaceEncodingAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleFaceEncodingAlreadyExists(
            FaceEncodingAlreadyExistsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), request.getRequestURI()));
    }

    /**
     * Handle no face detected in photo
     * Returns 422 Unprocessable Entity
     */
    @ExceptionHandler(NoFaceDetectedException.class)
    public ResponseEntity<ErrorResponse> handleNoFaceDetected(
            NoFaceDetectedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(ex.getMessage(), request.getRequestURI()));
    }

    /**
     * Handle face not recognized
     * Returns 404 Not Found
     */
    @ExceptionHandler(FaceNotRecognizedException.class)
    public ResponseEntity<ErrorResponse> handleFaceNotRecognized(
            FaceNotRecognizedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), request.getRequestURI()));
    }

    /**
     * Handle all other unexpected exceptions
     * Returns 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "An unexpected error occurred: " + ex.getMessage(),
                        request.getRequestURI()
                ));
    }
}
