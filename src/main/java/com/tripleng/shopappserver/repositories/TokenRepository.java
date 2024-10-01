package com.tripleng.shopappserver.repositories;

import com.tripleng.shopappserver.models.Token;
import com.tripleng.shopappserver.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findAllByUser(User user);

    Optional<Token> findByToken(String token);

    Optional<Token> findByRefreshToken(String refreshToken);

    Optional<Token> findByUserAndToken(User user, String token);

    Optional<Token> findByUserIdAndToken(Long id, String token);
}
