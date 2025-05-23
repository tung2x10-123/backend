package com.project.cloths.Service.Impl;

import com.project.cloths.Entity.User;
import com.project.cloths.Model.RequestModel;
import com.project.cloths.Model.ResponseModel;
import com.project.cloths.Service.AuthService;
import com.project.cloths.Util.JWTUtil;
import com.project.cloths.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private JavaMailSender mailSender; // Inject bằng @Autowired

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public ResponseEntity<ResponseModel> register(RequestModel request) {
        Map<String, Object> bodyData = request.getBody();
        String username = (String) bodyData.get("username");
        String email = (String) bodyData.get("email");
        String password = (String) bodyData.get("password");

        if (userRepository.findByEmail(email).isPresent()) {
            return buildErrorResponse("47", "Email already exists!", "XTIERR00088", "RECORD");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        userRepository.save(user);

        return buildSuccessResponse("User registered successfully!");
    }

    @Override
    public ResponseEntity<ResponseModel> login(RequestModel request) {
        Map<String, Object> bodyData = request.getBody();

        String username = (String) bodyData.get("username");
        String email = (String) bodyData.get("email");
        String password = (String) bodyData.get("password");

        if (email == null || password == null) {
            return buildErrorResponse("400", "Email or password cannot be null!", "XTIERR00090", "REQUEST");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return buildErrorResponse("401", "Invalid credentials!", "XTIERR00089", "AUTH");
        }

        String token = jwtUtil.generateToken(email);
        return buildSuccessResponse(token);
    }

    @Override
    public void forgotPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("Email not found");
        }

        User user = userOptional.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        try {
            userRepository.save(user);
            logger.info("Successfully saved reset token for email {}: {}", email, token);
        } catch (Exception e) {
            logger.error("Failed to save reset token for email {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to save reset token");
        }
        sendResetEmail(user.getEmail(), token);
    }

    private void sendResetEmail(String email, String token) {
        try {
            String resetUrl = "https://workshopclothes.netlify.app/reset-password?token=" + token;
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("hello@demomailtrap.co");
            helper.setTo(email);
            helper.setSubject("Password Reset Request");
            helper.setText("Click vào đường link sau để đặt lại mật khẩu: " + resetUrl);
            mailSender.send(message);
        } catch (MessagingException e) {
            // Log chi tiết lỗi
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        } catch (Exception e) {
            // Bắt lỗi bất ngờ
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        try {
            Optional<User> userOptional = userRepository.findByResetToken(token);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetToken(null);
                userRepository.save(user);
                logger.info("Successfully reset password for token: {}", token);
            } else {
                logger.error("Invalid or expired reset token: {}", token);
                throw new RuntimeException("Invalid or expired reset token");
            }
        } catch (Exception e) {
            logger.error("Error in resetPassword for token {}: {}", token, e.getMessage(), e);
            throw e;
        }
    }

    private Map<String, Object> getRequestBody(RequestModel request) {
        return request.getBody().get("data") instanceof Map
                ? (Map<String, Object>) request.getBody().get("data")
                : new HashMap<>();
    }

    private ResponseEntity<ResponseModel> buildErrorResponse(String errorCode, String errorMessage, String errorType, String recordType) {
        Map<String, String> resCode = new HashMap<>();
        resCode.put("error_code", errorCode);
        resCode.put("error_desc", errorMessage);
        resCode.put("ref_code", null);
        resCode.put("ref_desc", null);

        Map<String, Object> data = new HashMap<>();
        data.put("ErrorMess", errorMessage);
        data.put("ErrorType", recordType);
        data.put("ErrorCode", errorType);

        return ResponseEntity.badRequest().body(new ResponseModel(resCode, data));
    }

    private ResponseEntity<ResponseModel> buildSuccessResponse(Object dataValue) {
        Map<String, String> resCode = new HashMap<>();
        resCode.put("error_code", "00");
        resCode.put("error_desc", "Success");
        resCode.put("ref_code", null);
        resCode.put("ref_desc", null);

        Map<String, Object> data = new HashMap<>();
        data.put("Result", dataValue);

        return ResponseEntity.ok(new ResponseModel(resCode, data));
    }
}