package com.smartfood.repository;

import com.smartfood.domain.User;

public interface UserRepository {

    void save(User user);

    User findByUsername(String username);

}