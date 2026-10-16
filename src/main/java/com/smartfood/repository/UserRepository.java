package com.smartfood.repository;

import com.smartfood.domain.User;
import java.util.HashMap;
import java.util.Map;

/** In-memory storage for users. */
public class UserRepository {
    private final Map<String, User> users = new HashMap<>();

    /** @param user user to save */
    public void save(User user) { users.put(user.getUsername(), user); }

    /**
     * @param username the username
     * @return the user or null if not found
     */
    public User findByUsername(String username) { return users.get(username); }
}