package com.nizz.ByteBridge.services;

import com.nizz.ByteBridge.exceptions.UserAlreadyExistsException;
import com.nizz.ByteBridge.dto.authentication.SignupRequest;
import com.nizz.ByteBridge.models.User;
import com.nizz.ByteBridge.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(SignupRequest request) throws UserAlreadyExistsException {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        if (userRepository.existsByUserName(request.getUserName())) {
            throw new UserAlreadyExistsException("Provided user name is already taken");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserName(request.getUserName());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setCreatedAt(String.valueOf(LocalDateTime.now()));
        user.setUpdatedAt(String.valueOf(LocalDateTime.now()));

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUserName(username);
    }
}
