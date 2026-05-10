package com.attendance.face.controller;

import com.attendance.face.dto.response.FaceEncodingResponse;
import com.attendance.face.dto.response.RecognitionResponse;
import com.attendance.face.entity.Attendance;
import com.attendance.face.entity.Student;
import com.attendance.face.service.AttendanceService;
import com.attendance.face.service.FaceRecognitionClient;
import com.attendance.face.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
public class FaceRecognitionController {
    private final FaceRecognitionClient faceRecognitionClient;
    private final StudentService studentService;
    private final AttendanceService attendanceService;

    @Autowired
    public FaceRecognitionController(
            FaceRecognitionClient faceRecognitionClient,
            StudentService studentService,
            AttendanceService attendanceService) {
        this.faceRecognitionClient = faceRecognitionClient;
        this.studentService = studentService;
        this.attendanceService = attendanceService;
    }


    @PostMapping("/upload/{studentId}")
    public ResponseEntity<Map<String, Object>> uploadStudentPhoto(
            @PathVariable Long studentId,
            @RequestParam("photo") MultipartFile photo) throws IOException {


        if (photo.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Photo file is required");
            return ResponseEntity.badRequest().body(error);
        }


        String imageBase64 = Base64.getEncoder().encodeToString(photo.getBytes());


        FaceEncodingResponse response = faceRecognitionClient.createEncoding(studentId, imageBase64);


        if (response.isSuccess()) {
            studentService.updateFaceEncoding(studentId, response.getEncodingPath());
        }

        // Return response
        Map<String, Object> result = new HashMap<>();
        result.put("success", response.isSuccess());
        result.put("message", response.getMessage());
        result.put("facesDetected", response.getFacesDetected());

        if (!response.isSuccess()) {
            result.put("error", response.getError());
        }

        return ResponseEntity.ok(result);
    }


//    @PostMapping("/recognize")
//    public ResponseEntity<Map<String, Object>> recognizeAndMarkAttendance(
//            @RequestParam(required = false, defaultValue = "webcam") String captureSource,
//            @RequestParam(required = false) String sessionId) {
//
//
//        if (sessionId == null || sessionId.isEmpty()) {
//            sessionId = "auto-" + System.currentTimeMillis();
//        }
//
//
//        List<Student> activeStudents = studentService.getActiveStudents();
//
//        // Call Python service for recognition
//        RecognitionResponse response = faceRecognitionClient.recognizeFaces(
//                activeStudents,
//                captureSource,
//                sessionId
//        );
//
//        // Mark attendance for recognized students
//        List<Map<String, Object>> attendanceResults = new ArrayList<>();
//
//        for (RecognitionResponse.RecognizedStudent recognized : response.getRecognizedStudents()) {
//            try {
//                Attendance attendance = attendanceService.markAttendance(
//                        recognized.getStudentId(),
//                        captureSource,
//                        sessionId
//                );
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("studentId", recognized.getStudentId());
//                result.put("studentNumber", recognized.getStudentNumber());
//                result.put("confidence", recognized.getConfidence());
//                result.put("attendanceMarked", true);
//                result.put("attendanceId", attendance.getId());
//
//                attendanceResults.add(result);
//
//            } catch (Exception e) {
//                // Student might have already been marked (duplicate)
//                Map<String, Object> result = new HashMap<>();
//                result.put("studentId", recognized.getStudentId());
//                result.put("studentNumber", recognized.getStudentNumber());
//                result.put("confidence", recognized.getConfidence());
//                result.put("attendanceMarked", false);
//                result.put("error", e.getMessage());
//
//                attendanceResults.add(result);
//            }
//        }
//
//        // Build response
//        Map<String, Object> result = new HashMap<>();
//        result.put("success", response.isSuccess());
//        result.put("timestamp", response.getTimestamp());
//        result.put("totalFacesDetected", response.getTotalFacesDetected());
//        result.put("recognizedCount", response.getRecognizedStudents().size());
//        result.put("unknownFaces", response.getUnknownFaces());
//        result.put("attendanceResults", attendanceResults);
//        result.put("processingTimeMs", response.getProcessingTimeMs());
//
//        return ResponseEntity.ok(result);
//    }


    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkServiceHealth() {
        Map<String, Object> health = faceRecognitionClient.getServiceHealth();

        boolean isHealthy = "UP".equals(health.get("status"));

        return isHealthy
                ? ResponseEntity.ok(health)
                : ResponseEntity.status(503).body(health);
    }
}

