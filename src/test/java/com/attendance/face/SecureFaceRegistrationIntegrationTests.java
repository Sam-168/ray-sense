package com.attendance.face;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.attendance.face.dto.response.FaceEncodingResponse;
import com.attendance.face.entity.Student;
import com.attendance.face.service.FaceRecognitionClient;
import com.attendance.face.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecureFaceRegistrationIntegrationTests {

    private static final long STUDENT_ID = 42L;
    private static final byte[] PNG_HEADER = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private FaceRecognitionClient faceRecognitionClient;

    private Student activeStudent;

    @BeforeEach
    void setUp() {
        activeStudent = new Student();
        activeStudent.setId(STUDENT_ID);
        activeStudent.setIsActive(true);
    }

    @Test
    void adminCanRegisterAValidatedFace() throws Exception {
        String token = login("admin@attendance.com", "admin123");
        when(studentService.getStudentById(STUDENT_ID)).thenReturn(activeStudent);
        when(faceRecognitionClient.createEncoding(org.mockito.ArgumentMatchers.eq(STUDENT_ID), anyString()))
                .thenReturn(new FaceEncodingResponse(
                        true, STUDENT_ID, "encodings/42.dat", 1, "Face registered", null));

        mockMvc.perform(multipart("/upload/{studentId}", STUDENT_ID)
                        .file(validPng())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.facesDetected").value(1));

        verify(studentService).updateFaceEncoding(STUDENT_ID, "encodings/42.dat");
    }

    @Test
    void studentCannotRegisterOrReplaceFaceEncodings() throws Exception {
        String token = login("john@student.com", "password123");

        mockMvc.perform(multipart("/upload/{studentId}", STUDENT_ID)
                        .file(validPng())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(faceRecognitionClient);
        verifyNoInteractions(studentService);
    }

    @Test
    void existingFaceRequiresExplicitReplacement() throws Exception {
        String token = login("admin@attendance.com", "admin123");
        activeStudent.setFaceEncodingPath("encodings/existing.dat");
        when(studentService.getStudentById(STUDENT_ID)).thenReturn(activeStudent);

        mockMvc.perform(multipart("/upload/{studentId}", STUDENT_ID)
                        .file(validPng())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isConflict());

        verifyNoInteractions(faceRecognitionClient);
        verify(studentService, never()).updateFaceEncoding(STUDENT_ID, "encodings/existing.dat");
    }

    @Test
    void explicitReplacementIsAllowedForAdmin() throws Exception {
        String token = login("admin@attendance.com", "admin123");
        activeStudent.setFaceEncodingPath("encodings/existing.dat");
        when(studentService.getStudentById(STUDENT_ID)).thenReturn(activeStudent);
        when(faceRecognitionClient.createEncoding(org.mockito.ArgumentMatchers.eq(STUDENT_ID), anyString()))
                .thenReturn(new FaceEncodingResponse(
                        true, STUDENT_ID, "encodings/replaced.dat", 1, "Face replaced", null));

        mockMvc.perform(multipart("/upload/{studentId}", STUDENT_ID)
                        .file(validPng())
                        .param("replace", "true")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        verify(studentService).updateFaceEncoding(STUDENT_ID, "encodings/replaced.dat");
    }

    @Test
    void spoofedImageContentIsRejectedBeforeCallingFaceService() throws Exception {
        String token = login("admin@attendance.com", "admin123");
        MockMultipartFile spoofedPhoto = new MockMultipartFile(
                "photo", "not-a-face.png", MediaType.IMAGE_PNG_VALUE, "not an image".getBytes());

        mockMvc.perform(multipart("/upload/{studentId}", STUDENT_ID)
                        .file(spoofedPhoto)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isUnprocessableEntity());

        verifyNoInteractions(faceRecognitionClient);
        verifyNoInteractions(studentService);
    }

    @Test
    void mismatchedFaceServiceIdentityIsRejected() throws Exception {
        String token = login("admin@attendance.com", "admin123");
        when(studentService.getStudentById(STUDENT_ID)).thenReturn(activeStudent);
        when(faceRecognitionClient.createEncoding(org.mockito.ArgumentMatchers.eq(STUDENT_ID), anyString()))
                .thenReturn(new FaceEncodingResponse(
                        true, 99L, "encodings/99.dat", 1, "Face registered", null));

        mockMvc.perform(multipart("/upload/{studentId}", STUDENT_ID)
                        .file(validPng())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isUnprocessableEntity());

        verify(studentService, never()).updateFaceEncoding(
                org.mockito.ArgumentMatchers.anyLong(), anyString());
    }

    private MockMultipartFile validPng() {
        return new MockMultipartFile(
                "photo", "face.png", MediaType.IMAGE_PNG_VALUE, PNG_HEADER);
    }

    private String login(String email, String password) throws Exception {
        String body = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
