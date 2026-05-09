package com.attendance.face.controller;

import com.attendance.face.dto.AttendanceMarkRequest;
import com.attendance.face.dto.AttendanceResponse;
import com.attendance.face.entity.Attendance;
import com.attendance.face.entity.AttendanceSession;
import com.attendance.face.entity.AttendanceStatus;
import com.attendance.face.entity.Student;
import com.attendance.face.exception.DuplicateAttendanceException;
import com.attendance.face.exception.StudentNotFoundException;
import com.attendance.face.repository.AttendanceRepository;
import com.attendance.face.repository.AttendanceSessionRepository;
import com.attendance.face.repository.StudentRepository;
import com.attendance.face.service.AttendanceService;
import com.attendance.face.service.JwtService;
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
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final StudentService studentService;
    private final RestTemplate restTemplate;
    private final String pythonServiceUrl;
    private final JwtService jwtService;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository sessionRepository;


    @Autowired
    public AttendanceController(AttendanceService attendanceService,
                                AttendanceSessionRepository sessionRepository,
                                JwtService jwtService,
                                StudentRepository studentRepository,
                                AttendanceRepository attendanceRepository,
                                StudentService studentService,
                                RestTemplate restTemplate,
                                @Value("${face.recognition.service.url}")
                                    String pythonServiceUrl){

        this.attendanceService = attendanceService;
        this.studentService = studentService;
        this.restTemplate = restTemplate;
        this.pythonServiceUrl = pythonServiceUrl;
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.jwtService = jwtService;
        this.sessionRepository = sessionRepository;
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

    @GetMapping("/my-attendance")
    public ResponseEntity<Map<String, Object>> getMyAttendance(
            @RequestHeader("Authorization") String authHeader) {

        // Extract token and get student
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);

        // Find student by userId
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));

        // Get all attendance records
        List<Attendance> records = attendanceRepository.findByStudent(student);

        // Calculate stats
        long totalPresent = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        // Build response
        List<AttendanceResponse> attendanceList = records.stream()
                .map(AttendanceResponse::new)
                .sorted((a, b) -> b.getDate().compareTo(a.getDate())) // newest first
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("studentName", student.getFullName());
        response.put("studentNumber", student.getStudentNumber());
        response.put("classId", student.getClassId());
        response.put("totalPresent", totalPresent);
        response.put("totalRecords", records.size());
        response.put("attendancePercentage",
                records.isEmpty() ? 0 : Math.round((totalPresent * 100.0) / records.size()));
        response.put("records", attendanceList);

        return ResponseEntity.ok(response);
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
    /**
     * Mark attendance using face recognition
     * POST /sessions/{sessionId}/mark-by-face
     */
    @PostMapping("/sessions/{sessionId}/mark-by-face")
    public ResponseEntity<Map<String, Object>> markAttendanceByFace(
            @PathVariable Long sessionId,
            @RequestParam("photo") MultipartFile photo,
            @RequestHeader("Authorization") String authHeader) throws IOException {

        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException("Photo is required");
        }

        // Get student from token
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));

        // Validate session is still active
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.isActive()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "This session has ended");
            return ResponseEntity.badRequest().body(error);
        }

        // Convert photo to base64
        String imageBase64 = Base64.getEncoder().encodeToString(photo.getBytes());

        // Get active students with face encodings in this section
        List<Student> sectionStudents = new ArrayList<>(session.getSection().getStudents())
                .stream()
                .filter(s -> s.getFaceEncodingPath() != null)
                .collect(Collectors.toList());

        // Build known encodings for Python
        List<Map<String, Object>> knownEncodings = sectionStudents.stream()
                .map(s -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("studentId", s.getId());
                    map.put("encodingPath", s.getFaceEncodingPath());
                    return map;
                })
                .collect(Collectors.toList());

        // Call Python service
        String url = pythonServiceUrl + "/recognize-face";

        Map<String, Object> pythonRequest = new HashMap<>();
        pythonRequest.put("imageBase64", imageBase64);
        pythonRequest.put("knownEncodings", knownEncodings);

        try {
            ResponseEntity<Map> pythonResponse = restTemplate.postForEntity(
                    url, pythonRequest, Map.class);

            Map<String, Object> result = pythonResponse.getBody();

            if (result == null) throw new RuntimeException("Empty response from face recognition");

            Boolean faceDetected = (Boolean) result.get("faceDetected");
            Boolean matched      = (Boolean) result.get("matched");

            if (!faceDetected) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "No face detected. Please try again.");
                return ResponseEntity.ok(response);
            }

            if (!matched) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Face not recognized. Please try again.");
                return ResponseEntity.ok(response);
            }

            // Get matched student ID
            Integer matchedIdInt = (Integer) result.get("studentId");
            Long matchedStudentId = matchedIdInt.longValue();

            // Verify matched student is the logged-in student
            if (!matchedStudentId.equals(student.getId())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Face does not match your registered profile.");
                return ResponseEntity.ok(response);
            }

            // Mark attendance for this session
            Attendance attendance = attendanceService.markAttendanceForSession(
                    student.getId(), sessionId, "student-face-scan");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Attendance marked successfully!");
            response.put("studentName", student.getFullName());
            response.put("sessionCode", session.getSessionCode());
            response.put("moduleName", session.getSection().getModule().getModuleName());
            response.put("time", attendance.getTime());

            return ResponseEntity.ok(response);

        } catch (DuplicateAttendanceException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "You have already marked attendance for this session.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    @GetMapping("/active-sessions")
    public ResponseEntity<List<Map<String, Object>>> getActiveSessions(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);

        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));

        List<AttendanceSession> activeSessions = sessionRepository
                .findActiveSessionsForStudent(student);

        List<Map<String, Object>> response = activeSessions.stream().map(session -> {
            // Check if student already marked attendance
            boolean alreadyMarked = attendanceRepository
                    .existsByStudentAndSession(student, session);

            Map<String, Object> map = new HashMap<>();
            map.put("sessionId", session.getId());
            map.put("sessionCode", session.getSessionCode());
            map.put("sectionName", session.getSection().getFullSectionName());
            map.put("moduleName", session.getSection().getModule().getModuleName());
            map.put("lecturerName", session.getCreatedBy().getFullName());
            map.put("startedAt", session.getStartedAt());
            map.put("autoCloseMinutes", session.getAutoCloseMinutes());
            map.put("alreadyMarked", alreadyMarked);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
 }

