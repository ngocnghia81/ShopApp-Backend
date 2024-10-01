package com.tripleng.shopappserver.services.serviceImpl;

import com.tripleng.shopappserver.Components.JwtTokenUltil;
import com.tripleng.shopappserver.dtos.UpdateUserDTO;
import com.tripleng.shopappserver.dtos.UserDTO;
import com.tripleng.shopappserver.dtos.UserLoginDTO;
import com.tripleng.shopappserver.exceptions.DataNotFoundException;
import com.tripleng.shopappserver.exceptions.ExpiredTokenException;
import com.tripleng.shopappserver.exceptions.PermissionDenyException;
import com.tripleng.shopappserver.models.Role;
import com.tripleng.shopappserver.models.Token;
import com.tripleng.shopappserver.models.User;
import com.tripleng.shopappserver.repositories.RoleRepository;
import com.tripleng.shopappserver.repositories.TokenRepository;
import com.tripleng.shopappserver.repositories.UserRepository;
import com.tripleng.shopappserver.response.UserResponse;
import com.tripleng.shopappserver.services.ITokenService;
import com.tripleng.shopappserver.services.IUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUltil jwtTokenUltil;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final ITokenService tokenService;

    @Override
    @Transactional
    public User createUser(UserDTO user) throws DataNotFoundException, PermissionDenyException {
        String phoneNumber = user.getPhoneNumber();
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }
        User newUser = User.builder()
                .phoneNumber(phoneNumber)
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .fullName(user.getFullName())
                .googleAccountId(user.getGoogleAccountId())
                .facebookAccountId(user.getFacebookAccountId())
                .build();
        Role role = roleRepository.findById(user.getRoleId()).orElseThrow(() -> new DataNotFoundException("Role not found"));
        if (Objects.equals(role.getName().toUpperCase(), Role.ADMIN)) {
            throw new PermissionDenyException("You cannot register an admin account!");
        }
        if (user.getGoogleAccountId() == 0 && user.getFacebookAccountId() == 0) {
            String password = passwordEncoder.encode(user.getPassword());
            newUser.setPassword(password);
        }
        newUser.setRoleId(role);
        newUser.setActive(true);
        return userRepository.save(newUser);
    }

    @Override
    @Transactional
    public String login(UserLoginDTO userLoginDTO) throws DataNotFoundException {
        User user = userRepository.findByPhoneNumber(userLoginDTO.getPhoneNumber()).orElseThrow(() -> new DataNotFoundException(
                "Invalid phone number/password"));

        // Check password
        if (user.getFacebookAccountId() == 0 && user.getGoogleAccountId() == 0) {
            if (!passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Invalid phone number/password");
            }
        }
        Role role = roleRepository.findById(userLoginDTO.getRoleId()).orElseThrow(() -> new DataNotFoundException(
                "Role not found with id: " + userLoginDTO.getRoleId()));

        if (!Objects.equals(user.getRoleId().getId(), userLoginDTO.getRoleId())) {
            throw new BadCredentialsException("Invalid role/id");
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userLoginDTO.getPhoneNumber(), userLoginDTO.getPassword());
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUltil.generateToken(user);
    }

    @Override
    public UserResponse getUserDetailsFromToken(String token) throws DataNotFoundException, ExpiredTokenException {
        if (token.contains("Bearer")) {
            token = token.substring(7);
        }
        if (jwtTokenUltil.isTokenExpired(token)) {
            Token existingToken = tokenRepository.findByToken(token).orElseThrow(() -> new DataNotFoundException("Token not found"));
            UserResponse userResponse = UserResponse.formUser(existingToken.getUser());
//            if (tokenService.refreshToken(userResponse, existingToken.getRefreshToken()) == null) {
//                throw new ExpiredTokenException("Token expired");
//            }
        }

        String phoneNumber = jwtTokenUltil.extractPhoneNumber(token);
        if (phoneNumber != null) {
            return userRepository.findByPhoneNumber(phoneNumber).map(UserResponse::formUser)
                    .orElseThrow(() -> new DataNotFoundException("User not found with phone number: " + phoneNumber));
        }
        return null;
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserDTO updateUserDTO) throws DataNotFoundException {
        User user = userRepository.findById(id).orElseThrow(() -> new DataNotFoundException("User not found with id: " + id));

        if (updateUserDTO.getAddress() != null) user.setAddress(updateUserDTO.getAddress());
        if (updateUserDTO.getDateOfBirth() != null) user.setDateOfBirth(updateUserDTO.getDateOfBirth());
        if (updateUserDTO.getFullname() != null) user.setFullName(updateUserDTO.getFullname());
        if (updateUserDTO.getPhoneNumber() != null) user.setPhoneNumber(updateUserDTO.getPhoneNumber());
        if (updateUserDTO.getEmail() != null) user.setEmail(updateUserDTO.getEmail());
        if (updateUserDTO.getPassword() != null) user.setPassword(passwordEncoder.encode(updateUserDTO.getPassword()));
        userRepository.save(user);
        return UserResponse.formUser(user);
    }

    @Override
    public UserResponse getUserDetailsFromRefreshToken(String refreshToken) throws DataNotFoundException, ExpiredTokenException {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken).orElse(null);
        if (existingToken != null) {
            return getUserDetailsFromToken(existingToken.getToken());
        }
        return null;
    }
}
