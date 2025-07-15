package com.nizz.ByteBridge.services;

import com.nizz.ByteBridge.dto.authentication.LoginRequest;
import com.nizz.ByteBridge.dto.authentication.LoginResponse;
import com.nizz.ByteBridge.models.User;
import com.nizz.ByteBridge.repositories.UserRepository;
import com.nizz.ByteBridge.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest loginRequest) throws BadCredentialsException {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            ));

            String token = jwtUtil.generateToken(loginRequest.getUsername());

            Optional<User> userOptional = userRepository.findByUserName(loginRequest.getUsername());
            User user = userOptional.orElseThrow(() -> new RuntimeException("User not found"));

            user.setPassword(null);
            return new LoginResponse(token, user);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    public User getCurrentUser(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
