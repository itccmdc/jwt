package com.example.jwt.web.controller;

import com.example.jwt.domain.RefreshToken;
import com.example.jwt.domain.User;
import com.example.jwt.repository.RefreshTokenRepository;
import com.example.jwt.repository.UserRepository;
import com.example.jwt.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 1) 회원가입
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.getEmail() == null || req.getPassword() == null || req.getName() == null) {
            return ResponseEntity.badRequest().body(Map.of("code", "VALIDATION_ERROR", "message", "입력값 검증 실패"));
        }

        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("code", "DUPLICATE_EMAIL", "message", "이메일 중복"));
        }

        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .role("USER")
                .provider("local")
                .build();
        userRepository.save(user);

        return ResponseEntity.status(201).body("가입 성공");
    }

    // 2) 로그인 (ID/PW)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<User> uOpt = userRepository.findByEmail(req.getEmail());
        if (uOpt.isEmpty() || !passwordEncoder.matches(req.getPassword(), uOpt.get().getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("code", "AUTH_INVALID_CREDENTIALS", "message", "이메일/비밀번호 불일치"));
        }
        User user = uOpt.get();

        // create refresh token record (jti)
        String tokenId = UUID.randomUUID().toString();
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .tokenId(tokenId)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(14))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        System.out.println("userId: " + user.getId());
        System.out.println("email: " + user.getEmail());
        System.out.println("role: " + user.getRole());
        System.out.println("passwordHash: " + user.getPasswordHash());
        System.out.println("provider: " + user.getProvider());
        System.out.println("providerId: " + user.getProviderId());
        System.out.println("tokenVersion: " + user.getTokenVersion());

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user, tokenId);

        return ResponseEntity.ok(Map.of("accessToken", accessToken, "refreshToken", refreshToken, "expiresIn", 900));
    }

    // 3) 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest req) {
        try {
            Claims claims = jwtUtil.validateRefreshToken(req.getRefreshToken());
            String tokenId = claims.getId();
            System.out.println("claims: " + claims);
            System.out.println("tokenId: " + tokenId);
            Long userId = Long.parseLong(claims.getSubject());
            Optional<RefreshToken> rOpt = refreshTokenRepository.findByTokenId(tokenId);
            System.out.println("isEmpty: " + rOpt.isEmpty());
            System.out.println("isRevoked: " + rOpt.get().isRevoked());
            System.out.println("getExpiresAt isBefore: " + rOpt.get().getExpiresAt().isBefore(LocalDateTime.now()));
            if (rOpt.isEmpty() || rOpt.get().isRevoked() || rOpt.get().getExpiresAt().isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(401).body(Map.of("code", "AUTH_REFRESH_REVOKED", "message", "회수/무효화된 Refresh"));
            }

            // revoke old refresh token
            RefreshToken old = rOpt.get();
            old.setRevoked(true);
            refreshTokenRepository.save(old);

            // issue new refresh token
            User user = old.getUser();
            String newTokenId = UUID.randomUUID().toString();
            RefreshToken newRt = RefreshToken.builder()
                    .tokenId(newTokenId)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusDays(14))
                    .revoked(false)
                    .build();
            refreshTokenRepository.save(newRt);

            String newAccess = jwtUtil.generateAccessToken(user);
            String newRefresh = jwtUtil.generateRefreshToken(user, newTokenId);

            return ResponseEntity.ok(Map.of("accessToken", newAccess, "refreshToken", newRefresh));
        } catch (io.jsonwebtoken.JwtException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(401).body(Map.of("code", "AUTH_REFRESH_REVOKED", "message", "회수/무효화된 Refresh"));
        }
    }

    // 4) 로그아웃 (refresh token 무효화)
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshRequest req) {
        try {
            Claims claims = jwtUtil.validateRefreshToken(req.getRefreshToken());
            String tokenId = claims.getId();
            Optional<RefreshToken> rOpt = refreshTokenRepository.findByTokenId(tokenId);
            if (rOpt.isPresent()) {
                RefreshToken rt = rOpt.get();
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);
            }
            return ResponseEntity.noContent().build();
        } catch (io.jsonwebtoken.JwtException ex) {
            return ResponseEntity.status(400).body(Map.of("code", "VALIDATION_ERROR", "message", "입력값 검증 실패"));
        }
    }

    @Data static class RegisterRequest { private String email; private String password; private String name; }
    @Data static class LoginRequest { private String email; private String password; }
    @Data static class RefreshRequest { private String refreshToken; }
}
