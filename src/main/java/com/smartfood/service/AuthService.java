package com.smartfood.service;

import com.smartfood.domain.User;
import com.smartfood.repository.UserRepository;

/** Handles user authentication. */
public class AuthService {
    private final UserRepository userRepository;

    /** @param userRepository the user storage */
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Attempts to log in.
     * @param username the username
     * @param password the password
     * @return the logged-in user
     * @throws IllegalArgumentException if credentials are invalid
     */
    public User login(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        User user = userRepository.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return user;
    }
}