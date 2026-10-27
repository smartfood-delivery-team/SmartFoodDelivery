package com.smartfood.service;

import com.smartfood.domain.User;
import com.smartfood.repository.UserRepository;
import com.smartfood.security.PasswordHasher;

/**
 * Handles user authentication.
 */
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public AuthService(UserRepository userRepository,
                       PasswordHasher passwordHasher) {

        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    /**
     * Attempts to log in.
     *
     * @param username the username
     * @param password the plaintext password entered by the user
     * @return the authenticated user
     * @throws IllegalArgumentException if credentials are invalid
     */
    public User login(String username, String password) {

        if (username == null || password == null) {
            throw new IllegalArgumentException(
                    "Invalid username or password"
            );
        }

        User user = userRepository.findByUsername(username);

        if (user == null ||
                !passwordHasher.verify(password, user.getPasswordHash())) {

            throw new IllegalArgumentException(
                    "Invalid username or password"
            );
        }

        return user;
    }
}