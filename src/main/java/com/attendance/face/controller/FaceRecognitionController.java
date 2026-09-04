package com.attendance.face.controller;

import com.attendance.face.dto.response.FaceEncodingResponse;
import com.attendance.face.dto.response.RecognitionResponse;
import com.attendance.face.entity.Attendance;
import com.attendance.face.entity.Student;
import com.attendance.face.exception.FaceEncodingAlreadyExistsException;
import com.attendance.face.exception.FaceRegistrationException;
import com.attendance.face.exception.StudentNotActiveException;
import com.attendance.face.service.AttendanceService;
import com.attendance.face.service.FaceRecognitionClient;
import com.attendance.face.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
public class FaceRecognitionController {
    private static final long MAX_FACE_IMAGE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png");

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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadStudentPhoto(
            @PathVariable Long studentId,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(defaultValue = "false") boolean replace) throws IOException {

        if (photo == null || photo.isEmpty()) {
            throw new FaceRegistrationException("A face photo is required");
        }

        byte[] photoBytes = photo.getBytes();
        validateFaceImage(photo.getContentType(), photoBytes);

        Student student = studentService.getStudentById(studentId);
        if (!Boolean.TRUE.equals(student.getIsActive())) {
            throw new StudentNotActiveException("Cannot register a face for an inactive student");
        }
        if (hasFaceEncoding(student) && !replace) {
            throw new FaceEncodingAlreadyExistsException(
                    "This student already has a registered face. Use replace=true to replace it explicitly.");
        }

        String imageBase64 = Base64.getEncoder().encodeToString(photoBytes);

        FaceEncodingResponse response = faceRecognitionClient.createEncoding(studentId, imageBase64);

        if (!response.isSuccess()) {
            throw new FaceRegistrationException(
                    response.getMessage() != null ? response.getMessage() : "Face registration failed");
        }
        if (response.getStudentId() != null && !studentId.equals(response.getStudentId())) {
            throw new FaceRegistrationException("Face service returned a mismatched student identity");
        }
        if (response.getFacesDetected() == null || response.getFacesDetected() != 1) {
            throw new FaceRegistrationException("The photo must contain exactly one face");
        }
        if (response.getEncodingPath() == null || response.getEncodingPath().isBlank()) {
            throw new FaceRegistrationException("Face service did not return a valid encoding reference");
        }

        studentService.updateFaceEncoding(studentId, response.getEncodingPath());

        // Return response
        Map<String, Object> result = new HashMap<>();
        result.put("success", response.isSuccess());
        result.put("message", response.getMessage());
        result.put("facesDetected", response.getFacesDetected());

        return ResponseEntity.ok(result);
    }

    private void validateFaceImage(String contentType, byte[] photoBytes) {
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new FaceRegistrationException("Only JPEG and PNG face photos are supported");
        }
        if (photoBytes.length > MAX_FACE_IMAGE_BYTES) {
            throw new FaceRegistrationException("Face photo must not exceed 5 MB");
        }

        boolean validJpeg = photoBytes.length >= 3
                && (photoBytes[0] & 0xFF) == 0xFF
                && (photoBytes[1] & 0xFF) == 0xD8
                && (photoBytes[2] & 0xFF) == 0xFF;
        boolean validPng = photoBytes.length >= 8
                && (photoBytes[0] & 0xFF) == 0x89
                && photoBytes[1] == 0x50
                && photoBytes[2] == 0x4E
                && photoBytes[3] == 0x47
                && photoBytes[4] == 0x0D
                && photoBytes[5] == 0x0A
                && photoBytes[6] == 0x1A
                && photoBytes[7] == 0x0A;

        if (("image/jpeg".equalsIgnoreCase(contentType) && !validJpeg)
                || ("image/png".equalsIgnoreCase(contentType) && !validPng)) {
            throw new FaceRegistrationException("The uploaded file does not match its declared image type");
        }
    }

    private boolean hasFaceEncoding(Student student) {
        return student.getFaceEncodingPath() != null && !student.getFaceEncodingPath().isBlank();
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

