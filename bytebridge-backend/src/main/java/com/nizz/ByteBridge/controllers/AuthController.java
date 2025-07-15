package com.nizz.ByteBridge.controllers;

import com.nizz.ByteBridge.dto.authentication.LoginRequest;
import com.nizz.ByteBridge.dto.authentication.LoginResponse;
import com.nizz.ByteBridge.dto.authentication.SignupRequest;
import com.nizz.ByteBridge.exceptions.UserAlreadyExistsException;
import com.nizz.ByteBridge.models.User;
import com.nizz.ByteBridge.services.AuthService;
import com.nizz.ByteBridge.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest userData) {
        try {
            User user = userService.createUser(userData);
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (UserAlreadyExistsException e) {
            log.error("User creation error due to duplicate values ", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            log.error("User creation error: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            return ResponseEntity.ok(loginResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        } catch (Exception e) {
            log.error("Login error: ", e);
            return ResponseEntity.badRequest().body("Login failed");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Since we're using stateless JWT, logout is handled client-side
        // by removing the token from storage
        return ResponseEntity.ok("Logged out successfully");
    }
}
