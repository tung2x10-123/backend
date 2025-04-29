package com.project.cloths.Service;

import com.project.cloths.Model.RequestModel;
import com.project.cloths.Model.ResponseModel;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<ResponseModel> register(RequestModel request);
    ResponseEntity<ResponseModel> login(RequestModel request);

    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}
