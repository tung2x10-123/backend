package com.project.cloths.Controller;

import com.project.cloths.Dto.ForgotPasswordRequest;
import com.project.cloths.Model.RequestModel;
import com.project.cloths.Model.ResponseModel;
import com.project.cloths.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")// Cho phép React gọi API
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseModel> register(@RequestBody RequestModel request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseModel> login(@RequestBody RequestModel request) {
        return authService.login(request);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseModel> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        if (request == null || request.getBody() == null || request.getBody().getEmail() == null) {
            return ResponseEntity.badRequest().body(new ResponseModel(
                    Map.of("error_code", "400", "error_desc", "Email is required"),
                    Map.of("ErrorMess", "Email is required")
            ));
        }
        String email = request.getBody().getEmail();
        try {
            authService.forgotPassword(email);
            return ResponseEntity.ok(new ResponseModel(
                    Map.of("error_code", "00", "error_desc", "Success"),
                    Map.of("Result", "Password reset email sent")
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseModel(
                    Map.of("error_code", "500", "error_desc", "Failed to send email"),
                    Map.of("ErrorMess", "Failed to send password reset email: " + e.getMessage())
            ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password has been reset successfully");
    }
}
