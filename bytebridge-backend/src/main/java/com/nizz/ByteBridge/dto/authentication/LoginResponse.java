package com.nizz.ByteBridge.dto.authentication;

import com.nizz.ByteBridge.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private User user;
}
