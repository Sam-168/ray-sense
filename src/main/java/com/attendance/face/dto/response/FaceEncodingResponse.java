package com.attendance.face.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaceEncodingResponse {

    private boolean success;
    private Long studentId;
    private String encodingPath;
    private Integer facesDetected;
    private String message;
    private String error;
}
