package com.tripleng.shopappserver.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripleng.shopappserver.Components.LocalizationUtils;
import com.tripleng.shopappserver.dtos.RefreshTokenDTO;
import com.tripleng.shopappserver.dtos.UpdateUserDTO;
import com.tripleng.shopappserver.dtos.UserDTO;
import com.tripleng.shopappserver.dtos.UserLoginDTO;
import com.tripleng.shopappserver.exceptions.ExpiredTokenException;
import com.tripleng.shopappserver.exceptions.PermissionDenyException;
import com.tripleng.shopappserver.models.Token;
import com.tripleng.shopappserver.models.User;
import com.tripleng.shopappserver.response.LoginResponse;
import com.tripleng.shopappserver.response.RegisterResponse;
import com.tripleng.shopappserver.response.UserResponse;
import com.tripleng.shopappserver.services.ITokenService;
import com.tripleng.shopappserver.services.IUserService;
import com.tripleng.shopappserver.utils.MessageKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final LocalizationUtils localizationUtils;
    private final ITokenService tokenService;
    private final ObjectMapper objectMapper;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result
    ) {
        RegisterResponse registerResponse = new RegisterResponse();

        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();

            registerResponse.setMessage(errorMessages.toString());
            return ResponseEntity.badRequest().body(registerResponse);
        }

        if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
            registerResponse.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH));
            return ResponseEntity.badRequest().body(registerResponse);
        }

        try {
            User user = userService.createUser(userDTO);
            registerResponse.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_SUCCESSFULLY));
            registerResponse.setUser(user);
            return ResponseEntity.ok(registerResponse);
        } catch (Exception e) {
            registerResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(registerResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO,
            BindingResult result,
            HttpServletRequest request
    ) {
        // Kiểm tra thông tin đăng nhập và sinh token
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(
                        LoginResponse.builder()
                                .message(errorMessages.toString())
                                .build()
                );
            }
            String token = userService.login(userLoginDTO);

            String userAgent = request.getHeader("User-Agent");
            UserResponse userResponse = userService.getUserDetailsFromToken(token);
            User user = objectMapper.convertValue(userResponse, User.class);
            Token tokenObject;
            if (userAgent != null && userAgent.toLowerCase().contains("mobile")) {
                tokenObject = tokenService.addToken(user, token, true);
            } else {
                tokenObject = tokenService.addToken(user, token, false);
            }
            // Trả về token trong response

            return ResponseEntity.ok(LoginResponse.builder()
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                    .token(token)
                    .refreshToken(tokenObject.getRefreshToken())
                    .build());
        } catch (Exception | ExpiredTokenException e) {
            return ResponseEntity.badRequest().body(
                    LoginResponse.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED, e.getMessage()))
                            .build()
            );
        }
    }

    @PostMapping("/details")
    public ResponseEntity<?> getUserDetails(@RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(userService.getUserDetailsFromToken(token));
        } catch (Exception | ExpiredTokenException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or  hasRole('ROLE_USER')")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @RequestBody UpdateUserDTO updateUserDTO, @RequestHeader("Authorization") String token) {
        try {
            if (!Objects.equals(id, userService.getUserDetailsFromToken(token).getId())) {
                throw new PermissionDenyException("You can only update your own account");
            }
            return ResponseEntity.ok(userService.updateUser(id, updateUserDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ExpiredTokenException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/refresh-token")
//    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO,
                                                      @RequestHeader("Authorization") String authorization,
                                                      BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(LoginResponse.builder().message(errorMessages.toString()).build());
            }
            UserResponse userFromRefreshToken = userService.getUserDetailsFromRefreshToken(refreshTokenDTO.getRefreshToken());
            if (authorization.contains("Bearer")) {
                authorization = authorization.substring(7);
            }
            UserResponse userFormToken = userService.getUserDetailsFromToken(authorization);
            if (!Objects.equals(userFromRefreshToken.getId(), userFormToken.getId())) {
                throw new PermissionDenyException("You can only refresh your own token");
            }
            Token token = tokenService.refreshToken(userFormToken, refreshTokenDTO.getRefreshToken());
            return ResponseEntity.ok(
                    LoginResponse.builder()
                            .message("Refresh token successfully")
                            .token(token.getToken())
                            .refreshToken(token.getRefreshToken()).build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(LoginResponse.builder().message(e.getMessage()).build());
        } catch (ExpiredTokenException e) {
            throw new RuntimeException(e);
        }
    }
}
