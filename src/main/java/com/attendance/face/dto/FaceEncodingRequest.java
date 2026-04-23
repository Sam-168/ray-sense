package com.attendance.face.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaceEncodingRequest {

    private Long studentId;
    private String imageBase64;
}
