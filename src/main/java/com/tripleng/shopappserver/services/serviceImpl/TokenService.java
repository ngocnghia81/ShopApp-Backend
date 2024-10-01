package com.tripleng.shopappserver.services.serviceImpl;

import com.tripleng.shopappserver.Components.JwtTokenUltil;
import com.tripleng.shopappserver.exceptions.DataNotFoundException;
import com.tripleng.shopappserver.models.Token;
import com.tripleng.shopappserver.models.User;
import com.tripleng.shopappserver.repositories.TokenRepository;
import com.tripleng.shopappserver.repositories.UserRepository;
import com.tripleng.shopappserver.response.UserResponse;
import com.tripleng.shopappserver.services.ITokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService implements ITokenService {

    private static final int MAX_TOKENS = 3;
    @Value("${jwt.expirationTime}")
    private int expiration;

    @Value("${jwt.refreshExpirationTime}")
    private int refreshExpiration;

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenUltil jwtTokenUltil;

    @Override
    public Token addToken(User user, String token, boolean isMobileDevice) {
        List<Token> userTokens = tokenRepository.findAllByUser(user);

        if (userTokens.size() >= MAX_TOKENS) {
            boolean hasNonMobileToken = !userTokens.stream().allMatch(Token::isMobileDevice);
            Token tokenToDelete;
            if (hasNonMobileToken) {
                tokenToDelete =
                        userTokens.stream().filter(t -> !t.isMobileDevice()).findFirst().orElse(userTokens.get(0));
            } else {
                tokenToDelete = userTokens.get(0);
            }
            tokenRepository.delete(tokenToDelete);
        }

        long expirationInSeconds = expiration;
        LocalDateTime expirationDate = LocalDateTime.now().plusSeconds(expirationInSeconds);
        Token newToken = Token.builder()
                .user(user)
                .expired(false)
                .revoked(false)
                .token(token)
                .tokenType("Bearer")
                .expirationDate(expirationDate)
                .isMobile(isMobileDevice)
                .build();

        newToken.setRefreshToken(UUID.randomUUID().toString());
        newToken.setRefreshTokenExpirationDate(LocalDateTime.now().plusSeconds(refreshExpiration));
        return tokenRepository.save(newToken);
    }

    @Override
    public Token refreshToken(UserResponse user, String refreshToken) throws DataNotFoundException {
        Token token = tokenRepository.findByRefreshToken(refreshToken).orElseThrow(() -> new RuntimeException(
                "Invalid Refresh Token"));

        User existingUser = token.getUser();
        if (!isRefreshTokenValid(existingUser, refreshToken)) {
            throw new DataNotFoundException("Refresh token is invalid");
        }
        String newToken = jwtTokenUltil.generateToken(existingUser);
        token.setToken(newToken);
        token.setExpirationDate(LocalDateTime.now().plusSeconds(expiration));
//        addToken(existingUser, newToken, token.isMobileDevice());
        token.setRefreshToken(UUID.randomUUID().toString());
        token.setRefreshTokenExpirationDate(LocalDateTime.now().plusSeconds(refreshExpiration));
        return tokenRepository.save(token);
    }

    @Override
    public boolean isRefreshTokenValid(User user, String token) {
        Token existingToken = tokenRepository.findByRefreshToken(token).orElse(null);
        if (existingToken == null) {
            return false;
        }

        // Kiểm tra token có thuộc về người dùng không
        if (!existingToken.getUser().getId().equals(user.getId())) {
            return false;
        }

        // Kiểm tra token có bị thu hồi không
        if (existingToken.isRevoked()) {
            return false;
        }

        // Kiểm tra token có hết hạn không
        if (existingToken.isExpired()) {
            return false;
        }

        // Kiểm tra refresh token có hết hạn không
        if (existingToken.getRefreshTokenExpirationDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        return true;
    }


}
