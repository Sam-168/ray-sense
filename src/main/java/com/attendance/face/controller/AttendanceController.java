package com.attendance.face.controller;

import com.attendance.face.dto.AttendanceMarkRequest;
import com.attendance.face.dto.AttendanceResponse;
import com.attendance.face.entity.Attendance;
import com.attendance.face.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService){
        this.attendanceService = attendanceService;
    }

    @PostMapping("/mark")
    public ResponseEntity<AttendanceResponse> markAttendance(@Valid @RequestBody AttendanceMarkRequest request){
        Attendance attendance = attendanceService.markAttendance(
                request.getStudentId(),
                request.getCaptureSource(),
                request.getSessionId()
        );
        AttendanceResponse response = new AttendanceResponse(attendance);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/today")
    public ResponseEntity<List<AttendanceResponse>> getTodayAttendance(){
        List<Attendance> attendances = attendanceService.getTodayAttendance();

        List<AttendanceResponse> responses = attendances.stream()
                .map(AttendanceResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        List<Attendance> attendances = attendanceService.getAttendanceByDate(date);

        List<AttendanceResponse> responses = attendances.stream()
                .map(AttendanceResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByStudent(@PathVariable Long studentId){
        List<Attendance> attendances = attendanceService.getAttendanceByStudent(studentId);

        List<AttendanceResponse> responses = attendances.stream()
                .map(AttendanceResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceBySession(@PathVariable String sessionId){
        List<Attendance> attendances = attendanceService.getAttendanceBySession(sessionId);

        List<AttendanceResponse> responses = attendances.stream()
                .map(AttendanceResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/student/{studentId}/stats")
    public ResponseEntity<Map<String, Object>> getAttendanceStats(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam int totalDays){
        long count = attendanceService.getAttendanceCount(studentId, startDate, endDate);
        double percentage = attendanceService.calculateAttendancePercentage(studentId, startDate, endDate, totalDays);

        Map<String, Object> stats = new HashMap<>();
        stats.put("studentId" , studentId);
        stats.put("startDate", startDate);
        stats.put("endDate", endDate);
        stats.put("daysPresent", count);
        stats.put("totalDays", totalDays);
        stats.put("attendancePercentage", percentage);

        return ResponseEntity.ok(stats);
    }
 }

