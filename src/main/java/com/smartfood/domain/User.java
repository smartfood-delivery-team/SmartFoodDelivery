package com.smartfood.domain;

/**
 * Represents a system user (customer or manager).
 */
public class User {
    private final String username;
    private final String password;
    private final Role role;

    /**
     * Creates a user.
     * @param username unique username
     * @param password user password
     * @param role the role of the user
     */
    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /** @return the username */
    public String getUsername() { return username; }

    /** @return the password */
    public String getPassword() { return password; }

    /** @return the user's role */
    public Role getRole() { return role; }
}