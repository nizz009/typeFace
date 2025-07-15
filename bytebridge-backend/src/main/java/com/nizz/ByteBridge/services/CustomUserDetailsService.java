package com.nizz.ByteBridge.services;

import com.nizz.ByteBridge.models.User;
import com.nizz.ByteBridge.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUserName(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        User foundUser = user.get();
        return new org.springframework.security.core.userdetails.User(
                foundUser.getUserName(),
                foundUser.getPassword(),
                foundUser.isActive(),
                true,
                true,
                true,
                new ArrayList<>()
        );
    }
}
