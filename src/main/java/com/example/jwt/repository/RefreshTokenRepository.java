package com.example.jwt.repository;

import com.example.jwt.domain.RefreshToken;
import com.example.jwt.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenId(String tokenId);
    List<RefreshToken> findByUserAndRevokedFalse(User user);
}
