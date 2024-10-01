package com.tripleng.shopappserver.services;


import com.tripleng.shopappserver.exceptions.DataNotFoundException;
import com.tripleng.shopappserver.models.Token;
import com.tripleng.shopappserver.models.User;
import com.tripleng.shopappserver.response.UserResponse;

public interface ITokenService {
    Token addToken(User user, String token, boolean isMobileDevice);

    Token refreshToken(UserResponse user, String refreshToken) throws DataNotFoundException;

    boolean isRefreshTokenValid(User user, String token);
}
