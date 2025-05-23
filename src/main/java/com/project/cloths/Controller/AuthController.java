package com.project.cloths.Controller;

import com.project.cloths.Dto.ForgotPasswordRequest;
import com.project.cloths.Model.RequestModel;
import com.project.cloths.Model.ResponseModel;
import com.project.cloths.Service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")// Cho phép React gọi API
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
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

//    @PostMapping("/reset-password")
//    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
//        authService.resetPassword(token, newPassword);
//        return ResponseEntity.ok("Password has been reset successfully");
//    }
@PostMapping("/reset-password")
public ResponseEntity<ResponseModel> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
    try {
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok(new ResponseModel(
                Map.of("error_code", "00", "error_desc", "Success"),
                Map.of("Result", "Password has been reset successfully")
        ));
    } catch (RuntimeException e) {
        logger.error("Error in reset-password for token {}: {}", token, e.getMessage(), e);
        return ResponseEntity.badRequest().body(new ResponseModel(
                Map.of("error_code", "400", "error_desc", "Invalid request"),
                Map.of("ErrorMess", e.getMessage())
        ));
    } catch (Exception e) {
        logger.error("Unexpected error in reset-password for token {}: {}", token, e.getMessage(), e);
        return ResponseEntity.status(500).body(new ResponseModel(
                Map.of("error_code", "500", "error_desc", "Internal Server Error"),
                Map.of("ErrorMess", "Unexpected error occurred")
        ));
    }
}
}
