package com.tripleng.shopappserver.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserDTO {
    private String fullname;
    @JsonProperty("phone_number")
    private String phoneNumber;
    private String address;
    private String email;
    @JsonProperty("date_of_birth")
    private Date dateOfBirth;
    private String password;
}
