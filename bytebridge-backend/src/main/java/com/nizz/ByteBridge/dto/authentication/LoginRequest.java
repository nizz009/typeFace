package com.nizz.ByteBridge.dto.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "User Name is required")
    @JsonProperty("user_name")
    private String username;
    @NotBlank(message = "Password is required")
    private String password;
}
