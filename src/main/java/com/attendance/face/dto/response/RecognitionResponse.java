package com.attendance.face.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecognitionResponse {
    private boolean success;
    private LocalDateTime timestamp;
    private Integer totalFacesDetected;
    private List<RecognizedStudent> recognizedStudents;
    private Integer unknownFaces;
    private String message;
    private Long processingTimeMs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecognizedStudent {
        private Long studentId;
        private String studentNumber;
        private Double confidence;
        private BoundingBox boundingBox;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoundingBox {
        private Integer x;
        private Integer y;
        private Integer width;
        private Integer height;
    }
}
