package com.attendance.face.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCreateRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100,  message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Student number is required")
    @Size(min = 3, max = 50, message = "Student number must be between 3 and 50 characters")
    private String studentNumber;

    @Size(max = 50, message = "Class ID cannot 50 characters")
    private String classId;

    @NotBlank(message = "Photo is required")
    private String photo;

}
