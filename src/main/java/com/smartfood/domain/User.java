package com.smartfood.domain;

/**
 * Represents a system user (customer or manager).
 */
public class User {
    private final String username;
    private final String passwordHash;
    private final Role role;

    /**
     * Creates a user.
     * @param username unique username
     * @param passwordHash user passwordHash
     * @param role the role of the user
     */
    public User(String username, String passwordHash, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    /** @return the username */
    public String getUsername() { return username; }

    /** @return the password */
    public String getPasswordHash() {
        return passwordHash;
    }

    /** @return the user's role */
    public Role getRole() { return role; }
}