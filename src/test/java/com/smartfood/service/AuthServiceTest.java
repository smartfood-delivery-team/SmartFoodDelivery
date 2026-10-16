package com.smartfood.service;

import com.smartfood.domain.*;
import com.smartfood.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    private AuthService auth;

    @BeforeEach
    void setUp() {
        UserRepository repo = new UserRepository();
        repo.save(new User("lana", "1234", Role.CUSTOMER));
        auth = new AuthService(repo);
    }

    @Test
    void validCredentials_loginSuccess() {
        User u = auth.login("lana", "1234");
        assertEquals("lana", u.getUsername());
        assertEquals(Role.CUSTOMER, u.getRole());
    }

    @Test
    void wrongPassword_throws() {
        assertThrows(IllegalArgumentException.class, () -> auth.login("lana", "wrong"));
    }

    @Test
    void unknownUser_throws() {
        assertThrows(IllegalArgumentException.class, () -> auth.login("nobody", "1234"));
    }

    @Test
    void nullCredentials_throws() {
        assertThrows(IllegalArgumentException.class, () -> auth.login(null, null));
    }
    @Test
    void nullPassword_throws() {
        assertThrows(IllegalArgumentException.class, () -> auth.login("lana", null));
    }
}
