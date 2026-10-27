package com.smartfood.security;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    public String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }

        return BCrypt.checkpw(password, storedHash);
    }

}
