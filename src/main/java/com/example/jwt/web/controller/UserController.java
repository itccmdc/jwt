package com.example.jwt.web.controller;

import com.example.jwt.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")  // 이 컨트롤러/메서드는 JWT 필요
    public ResponseEntity<?> me() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            User user = (User) principal;
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "roles", new String[]{user.getRole()}
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of("code", "AUTH_INVALID_CREDENTIALS", "message", "Unauthorized"));
        }
    }
}
