package com.attendance.face.dto.response;

import com.attendance.face.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserRole role;
    private Long userId;
    private String email;
    private String fullName;


    private Long studentId;
    private Long lecturerId;
    private Long adminId;
}
