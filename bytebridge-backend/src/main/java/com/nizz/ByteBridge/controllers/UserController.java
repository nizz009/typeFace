package com.nizz.ByteBridge.controllers;

import com.nizz.ByteBridge.dto.authentication.SignupRequest;
import com.nizz.ByteBridge.models.User;
import com.nizz.ByteBridge.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody SignupRequest userData) {
        try {
            User user = userService.createUser(userData);
            // Remove password from response for security
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("User creation error: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<User> getUser(@RequestParam String userName) {
        try {
            Optional<User> user = userService.findByUsername(userName);
            if (user.isPresent()) {
                User foundUser = user.get();
                foundUser.setPassword(null);
                return ResponseEntity.ok(foundUser);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Get user error: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{username}")
    public ResponseEntity<User> updateUser(@PathVariable String userName, @RequestBody User userData) {
        // TODO: Implement this
        userData.setPassword(null);
        return ResponseEntity.ok(userData);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String userName) {
        // TODO: Implement this
        return ResponseEntity.ok("User deleted successfully");
    }
}