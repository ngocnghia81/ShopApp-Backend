package com.tripleng.shopappserver.Components;

import com.tripleng.shopappserver.exceptions.DataNotFoundException;
import com.tripleng.shopappserver.models.Token;
import com.tripleng.shopappserver.models.User;
import com.tripleng.shopappserver.repositories.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUltil {

    private final TokenRepository tokenRepository;
    @Value("${jwt.expirationTime}")
    private Long expirationTime;

    @Value("${jwt.clockSkew}")
    private long clockSkew;  // Đơn vị: giây

    @Value("${jwt.secretKey}")
    private String secretKey;

    public String generateToken(User user) {
        // properties => claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("userId", user.getId());
        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getPhoneNumber())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expirationTime * 1000L))
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error generating JWT: " + e.getMessage());
        }
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .setAllowedClockSkewSeconds(clockSkew)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extracClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = this.getClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenExpired(String token) {
        return extracClaim(token, Claims::getExpiration).before(new Date());
    }


    public String extractPhoneNumber(String token) {
        return this.extracClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(User user, String token) throws DataNotFoundException {
        String phoneNumber = this.getClaimsFromToken(token).getSubject();
        Token existingToken = tokenRepository.findByToken(token).orElseThrow(() -> new DataNotFoundException("Token not found with token: " + token));
        if (existingToken == null || existingToken.isRevoked() || existingToken.isExpired()) {
            return false;
        }
        return user.getPhoneNumber().equals(phoneNumber) && !isTokenExpired(token);
    }
}

