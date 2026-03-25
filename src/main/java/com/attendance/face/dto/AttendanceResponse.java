package com.attendance.face.dto;

import com.attendance.face.entity.Attendance;
import com.attendance.face.entity.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AttendanceResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private LocalDate date;
    private LocalTime time;
    private AttendanceStatus status;
    private String sessionId;
    private String captureSource;
    private LocalDateTime createdAt;


    public AttendanceResponse(Attendance attendance) {
        this.id = attendance.getId();
        this.studentId = attendance.getStudent().getId();
        this.studentName = attendance.getStudent().getFullName();
        this.studentNumber = attendance.getStudent().getStudentNumber();
        this.date = attendance.getDate();
        this.time = attendance.getTime();
        this.status = attendance.getStatus();
        this.sessionId = attendance.getSessionId();
        this.captureSource = attendance.getCaptureSource();
        this.createdAt = attendance.getCreatedAt();
    }
}
