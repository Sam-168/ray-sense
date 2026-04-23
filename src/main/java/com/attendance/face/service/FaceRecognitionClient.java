package com.attendance.face.service;
import com.attendance.face.dto.FaceEncodingRequest;
import com.attendance.face.dto.RecognitionRequest;
import com.attendance.face.dto.response.FaceEncodingResponse;
import com.attendance.face.dto.response.RecognitionResponse;
import com.attendance.face.entity.Student;
import com.attendance.face.exception.FaceRecognitionServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FaceRecognitionClient {
    private final RestTemplate restTemplate;
    private final String pythonServiceUrl;

    public FaceRecognitionClient(
            RestTemplate restTemplate,
            @Value("${face.recognition.service.url}") String pythonServiceUrl) {
        this.restTemplate = restTemplate;
        this.pythonServiceUrl = pythonServiceUrl;
    }

    /**
     * Create face encoding from student photo
     */
    public FaceEncodingResponse createEncoding(Long studentId, String imageBase64) {
        String url = pythonServiceUrl + "/encode";

        FaceEncodingRequest request = new FaceEncodingRequest(studentId, imageBase64);

        try {
            ResponseEntity<FaceEncodingResponse> response = restTemplate.postForEntity(
                    url,
                    request,
                    FaceEncodingResponse.class
            );

            if (response.getBody() == null) {
                throw new FaceRecognitionServiceException("Empty response from face recognition service");
            }

            return response.getBody();

        } catch (RestClientException e) {
            throw new FaceRecognitionServiceException(
                    "Failed to communicate with face recognition service: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Recognize faces from webcam
     */
    public RecognitionResponse recognizeFaces(
            List<Student> activeStudents,
            String captureSource,
            String sessionId) {

        String url = pythonServiceUrl + "/recognize";

        // Convert active students to known encodings list
        List<RecognitionRequest.KnownStudent> knownEncodings = activeStudents.stream()
                .filter(student -> student.getFaceEncodingPath() != null)
                .map(student -> new RecognitionRequest.KnownStudent(
                        student.getId(),
                        student.getStudentNumber(),
                        student.getFaceEncodingPath()
                ))
                .collect(Collectors.toList());

        RecognitionRequest request = new RecognitionRequest(
                captureSource,
                sessionId,
                knownEncodings
        );

        try {
            ResponseEntity<RecognitionResponse> response = restTemplate.postForEntity(
                    url,
                    request,
                    RecognitionResponse.class
            );

            if (response.getBody() == null) {
                throw new FaceRecognitionServiceException("Empty response from face recognition service");
            }

            return response.getBody();

        } catch (RestClientException e) {
            throw new FaceRecognitionServiceException(
                    "Failed to communicate with face recognition service: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Check if Python service is healthy
     */
    public boolean isServiceHealthy() {
        String url = pythonServiceUrl + "/health";

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getBody() != null) {
                Object status = response.getBody().get("status");
                return "UP".equals(status);
            }

            return false;

        } catch (RestClientException e) {
            return false;
        }
    }

    /**
     * Get health details from Python service
     */
    public Map<String, Object> getServiceHealth() {
        String url = pythonServiceUrl + "/health";

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody() != null ? response.getBody() : new HashMap<>();

        } catch (RestClientException e) {
            Map<String, Object> errorHealth = new HashMap<>();
            errorHealth.put("status", "DOWN");
            errorHealth.put("error", e.getMessage());
            return errorHealth;
        }
    }
}
