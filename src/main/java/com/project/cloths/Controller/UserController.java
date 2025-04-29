package com.project.cloths.Controller;

import com.project.cloths.Entity.User;
import com.project.cloths.Service.Impl.UserServiceImpl;
import com.project.cloths.Util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    private final JWTUtil jwtUtil;

    @GetMapping("/me")
    public ResponseEntity<Optional<User>> getUserProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.getEmailFromToken(token);
        User user = userService.getUserByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(Optional.of(user));
        }
        return ResponseEntity.status(401).build();
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(@AuthenticationPrincipal UserDetails userDetails,
                                                  @RequestBody User updateRequest) {
        User updatedUser = userService.updateUser(userDetails.getUsername(),
                updateRequest.getEmail(),
                updateRequest.getPassword());
        return ResponseEntity.ok(updatedUser);
    }
}
