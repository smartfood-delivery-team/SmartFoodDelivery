package com.smartfood.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    void hashPasswordSuccessfully() {
        String hash = hasher.hash("123456");

        assertNotNull(hash);
        assertNotEquals("123456", hash);
    }

    @Test
    void correctPasswordReturnsTrue() {
        String hash = hasher.hash("123456");

        assertTrue(hasher.verify("123456", hash));
    }

    @Test
    void wrongPasswordReturnsFalse() {
        String hash = hasher.hash("123456");

        assertFalse(hasher.verify("wrong", hash));
    }

    @Test
    void emptyPasswordIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> hasher.hash("")
        );
    }
    @Test
    void samePasswordProducesDifferentHashes() {
        String hash1 = hasher.hash("123456");
        String hash2 = hasher.hash("123456");

        assertNotEquals(hash1, hash2);
        assertTrue(hasher.verify("123456", hash1));
        assertTrue(hasher.verify("123456", hash2));
    }
}