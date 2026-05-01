package com.attendance.face.controller;

import com.attendance.face.dto.AttendanceMarkRequest;
import com.attendance.face.dto.AttendanceResponse;
import com.attendance.face.entity.Attendance;
import com.attendance.face.entity.Student;
import com.attendance.face.service.AttendanceService;
import com.attendance.face.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final StudentService studentService;
    private final RestTemplate restTemplate;
    private final String pythonServiceUrl;

    @Autowired
    public AttendanceController(AttendanceService attendanceService,
                                StudentService studentService,
                                RestTemplate restTemplate,
                                @Value("${face.recognition.service.url}")
                                    String pythonServiceUrl){

        this.attendanceService = attendanceService;
        this.studentService = studentService;
        this.restTemplate = restTemplate;
        this.pythonServiceUrl = pythonServiceUrl;
    }

//    @PostMapping("/mark")
//    public ResponseEntity<AttendanceResponse> markAttendance(@Valid @RequestBody AttendanceMarkRequest request){
//        Attendance attendance = attendanceService.markAttendance(
//                request.getStudentId(),
//                request.getCaptureSource(),
//                request.getSessionId()
//        );
//        AttendanceResponse response = new AttendanceResponse(attendance);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }

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
    /**
     * Mark attendance using face recognition
     * POST /api/attendance/mark-by-face
     */
    @PostMapping("/mark-by-face")
    public ResponseEntity<AttendanceResponse> markAttendanceByFace(
            @RequestBody String photo,
            @RequestParam(required = false, defaultValue = "student-checkin") String captureSource
    ) {

        System.out.println("=== START: markAttendanceByFace ===");

        if (photo == null || photo.isBlank()) {
            throw new IllegalArgumentException("Photo is required");
        }

        System.out.println("Base64 photo received, length: " + photo.length());

        // The frontend already sends base64 → no need to re-encode
        String imageBase64 = photo;

        // Get all active students with encodings
        List<Student> activeStudents = studentService.getActiveStudents().stream()
                .filter(s -> s.getFaceEncodingPath() != null)
                .collect(Collectors.toList());

        System.out.println("Found " + activeStudents.size() + " active students with encodings");

        // Prepare known encodings list for Python
        List<Map<String, Object>> knownEncodings = activeStudents.stream()
                .map(student -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("studentId", student.getId());
                    map.put("encodingPath", student.getFaceEncodingPath());
                    return map;
                })
                .collect(Collectors.toList());

        // Call Python service
        String url = pythonServiceUrl + "/recognize-face";

        Map<String, Object> request = new HashMap<>();
        request.put("imageBase64", imageBase64);
        request.put("knownEncodings", knownEncodings);

        System.out.println("Calling Python service at: " + url);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            Map<String, Object> result = response.getBody();

            if (result == null) {
                throw new RuntimeException("Empty response from face recognition service");
            }

            Boolean faceDetected = (Boolean) result.get("faceDetected");
            Boolean matched = (Boolean) result.get("matched");

            if (!Boolean.TRUE.equals(faceDetected)) {
                throw new RuntimeException("No face detected in photo");
            }

            if (!Boolean.TRUE.equals(matched)) {
                throw new RuntimeException("Face not recognized");
            }

            Integer studentIdInt = (Integer) result.get("studentId");
            Long studentId = studentIdInt.longValue();

            Attendance attendance = attendanceService.markAttendance(
                    studentId,
                    captureSource,
                    null
            );

            AttendanceResponse attendanceResponse = new AttendanceResponse(attendance);

            System.out.println("=== END: SUCCESS ===");

            return ResponseEntity.status(HttpStatus.CREATED).body(attendanceResponse);

        } catch (RestClientException e) {
            throw new RuntimeException("Face recognition service error: " + e.getMessage());
        }
    }
 }

