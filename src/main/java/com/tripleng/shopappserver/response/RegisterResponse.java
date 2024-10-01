package com.tripleng.shopappserver.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripleng.shopappserver.models.User;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponse {
    @JsonProperty("message")
    private String message;

    @JsonProperty("user")
    private User user;
}
