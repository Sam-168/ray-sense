package com.attendance.face.controller;
import com.attendance.face.dto.LoginRequest;
import com.attendance.face.dto.StudentCreateRequest;
import com.attendance.face.dto.response.LoginResponse;
import com.attendance.face.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/register/student")
    public ResponseEntity<LoginResponse> registerStudent(
            @Valid @RequestBody StudentCreateRequest request) {
        LoginResponse response = authService.registerStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
