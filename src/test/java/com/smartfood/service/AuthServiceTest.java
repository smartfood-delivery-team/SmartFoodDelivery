package com.smartfood.service;

import com.smartfood.domain.*;
import com.smartfood.repository.InMemoryUserRepository;
import com.smartfood.repository.UserRepository;
import com.smartfood.security.PasswordHasher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService auth;

    @BeforeEach
    void setUp() {

        PasswordHasher hasher = new PasswordHasher();

        UserRepository repo = new InMemoryUserRepository();

        repo.save(new User(
                "lana",
                hasher.hash("1234"),
                Role.CUSTOMER
        ));

        // Initialize the class field, not a local variable
        auth = new AuthService(repo, hasher);
    }

    @Test
    void validCredentials_loginSuccess() {

        User u = auth.login("lana", "1234");

        assertEquals("lana", u.getUsername());
        assertEquals(Role.CUSTOMER, u.getRole());
    }

    @Test
    void wrongPassword_throws() {

        assertThrows(
                IllegalArgumentException.class,
                () -> auth.login("lana", "wrong")
        );
    }

    @Test
    void unknownUser_throws() {

        assertThrows(
                IllegalArgumentException.class,
                () -> auth.login("nobody", "1234")
        );
    }

    @Test
    void nullCredentials_throws() {

        assertThrows(
                IllegalArgumentException.class,
                () -> auth.login(null, null)
        );
    }

    @Test
    void nullPassword_throws() {

        assertThrows(
                IllegalArgumentException.class,
                () -> auth.login("lana", null)
        );
    }
    @Test
    void managerLoginSuccess() {
        PasswordHasher hasher = new PasswordHasher();

        UserRepository repo = new InMemoryUserRepository();

        repo.save(new User(
                "admin",
                hasher.hash("admin123"),
                Role.MANAGER
        ));

        AuthService managerAuth = new AuthService(repo, hasher);

        User user = managerAuth.login("admin", "admin123");

        assertEquals("admin", user.getUsername());
        assertEquals(Role.MANAGER, user.getRole());
    }
}