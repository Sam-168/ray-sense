package com.attendance.face.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecognitionRequest {
    private String captureSource;
    private String sessionId;
    private List<KnownStudent> knownEncodings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnownStudent {
        private Long studentId;
        private String studentNumber;
        private String encodingPath;
    }
}
