package com.tripleng.shopappserver.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenDTO {
    @NotEmpty(message = "Refresh token cannot be empty")
//    @JsonProperty("refresh_token")
    private String refreshToken;
}
