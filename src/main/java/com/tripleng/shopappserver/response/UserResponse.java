package com.tripleng.shopappserver.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripleng.shopappserver.models.Role;
import com.tripleng.shopappserver.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    @JsonProperty("phone_number")
    private String phoneNumber;
    private String address;
    private String email;
    @JsonProperty("data_of_birth")
    private Date dateOfBirth;
    @JsonProperty("facebook_account_id")
    private int facebookAccountId;
    @JsonProperty("google_account_id")
    private int googleAccountId;
    @JsonProperty("role_id")
    private Role role;
    private String password;

    public static UserResponse formUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .password(user.getPassword())
                .address(user.getAddress())
                .email(user.getEmail())
                .dateOfBirth(user.getDateOfBirth())
                .facebookAccountId(user.getFacebookAccountId())
                .googleAccountId(user.getGoogleAccountId())
                .role(user.getRoleId())
                .build();
    }

}
