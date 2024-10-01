package com.tripleng.shopappserver.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token")
    private String token;

    @Column(name = "token_type", length = 50, nullable = true)
    private String tokenType;

    private boolean revoked;
    private boolean expired;

    @JsonProperty("expiration_date")
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;
    @JsonProperty("is_mobile")
    @Column(name = "is_mobile")
    private boolean isMobile;

    @JsonProperty("refresh_token")
    @Column(name = "refresh_token")
    private String refreshToken;
    @Column(name = "refresh_expiration_date")
    @JsonProperty("refresh_expiration_date")
    private LocalDateTime refreshTokenExpirationDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public boolean isMobileDevice() {
        return tokenType.equals("mobile");
    }
}
