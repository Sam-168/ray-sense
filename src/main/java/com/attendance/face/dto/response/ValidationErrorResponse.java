package com.attendance.face.dto.response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidationErrorResponse extends ErrorResponse{
    private Map<String, String> fieldErrors;

    public ValidationErrorResponse(String message) {
        super(message);
        this.fieldErrors = new HashMap<>();
    }

    public ValidationErrorResponse(String message, String path) {
        super(message, path);
        this.fieldErrors = new HashMap<>();
    }

    public void addFieldError(String field, String errorMessage) {
        this.fieldErrors.put(field, errorMessage);
    }
}
