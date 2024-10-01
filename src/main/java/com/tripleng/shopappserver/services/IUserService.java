package com.tripleng.shopappserver.services;

import com.tripleng.shopappserver.dtos.UpdateUserDTO;
import com.tripleng.shopappserver.dtos.UserDTO;
import com.tripleng.shopappserver.dtos.UserLoginDTO;
import com.tripleng.shopappserver.exceptions.DataNotFoundException;
import com.tripleng.shopappserver.exceptions.ExpiredTokenException;
import com.tripleng.shopappserver.exceptions.PermissionDenyException;
import com.tripleng.shopappserver.models.User;
import com.tripleng.shopappserver.response.UserResponse;

public interface IUserService {
    User createUser(UserDTO user) throws DataNotFoundException, PermissionDenyException;

    String login(UserLoginDTO userLoginDTO) throws DataNotFoundException;

    UserResponse getUserDetailsFromToken(String token) throws DataNotFoundException, ExpiredTokenException;

    UserResponse updateUser(Long id, UpdateUserDTO updateUserDTO) throws DataNotFoundException;

    UserResponse getUserDetailsFromRefreshToken(String refreshToken) throws DataNotFoundException, ExpiredTokenException;
}
