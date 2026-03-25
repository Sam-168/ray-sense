package com.attendance.face.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceMarkRequest {

    @NotNull(message = "Student ID is required")

    private Long studentId;

    private String captureSource;

    private String sessionId;
}
