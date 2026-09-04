package com.attendance.face;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthorizationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void publicHealthDoesNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void studentCannotListOrModifyOtherStudents() throws Exception {
        String token = login("john@student.com", "password123");

        mockMvc.perform(get("/api/students")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        MockMultipartFile photo = new MockMultipartFile(
                "photo", "face.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3});
        mockMvc.perform(multipart("/upload/{studentId}", 999L)
                        .file(photo)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCanUseStudentAttendanceEndpointsButNotGlobalAttendance() throws Exception {
        String token = login("john@student.com", "password123");

        mockMvc.perform(get("/api/attendance/active-sessions")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/attendance/today")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void lecturerCanUseLecturerRoutesButNotAdminRoutes() throws Exception {
        String token = login("lecturer@attendance.com", "lecturer123");

        mockMvc.perform(get("/api/lecturer/sections")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanManageStudentsAndUseAdminRoutes() throws Exception {
        String token = login("admin@attendance.com", "admin123");

        mockMvc.perform(get("/api/students")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void unclassifiedEndpointsAreDenied() throws Exception {
        String token = login("admin@attendance.com", "admin123");

        mockMvc.perform(get("/api/unclassified")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    private String login(String email, String password) throws Exception {
        String body = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        String response = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/auth/login")
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
