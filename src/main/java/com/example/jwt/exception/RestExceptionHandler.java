package com.example.jwt.exception;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> handleJwt(JwtException ex, HttpServletRequest req) {
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "path", req.getRequestURI(),
                "status", 401,
                "code", "AUTH_INVALID_CREDENTIALS",
                "message", "이메일/비밀번호 불일치"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex, HttpServletRequest req) {
        ex.printStackTrace();
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "path", req.getRequestURI(),
                "status", 500,
                "code", "INTERNAL_ERROR",
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
