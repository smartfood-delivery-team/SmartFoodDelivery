package com.smartfood.repository;

import com.smartfood.database.DatabaseManager;
import com.smartfood.domain.Role;
import com.smartfood.domain.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteUserRepository implements UserRepository {

    private final DatabaseManager databaseManager;

    public SqliteUserRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void save(User user) {

        String sql = """
                INSERT INTO users (username, password_hash, role)
                VALUES (?, ?, ?)
                """;

        try (
                Connection connection = databaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getRole().name());

            int rows = statement.executeUpdate();

            if (rows != 1) {
                throw new IllegalStateException("Failed to insert user");
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save user", e);
        }
    }

    @Override
    public User findByUsername(String username) {

        String sql = """
                SELECT username, password_hash, role
                FROM users
                WHERE username = ?
                """;

        try (
                Connection connection = databaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    String foundUsername = result.getString("username");
                    String storedHash = result.getString("password_hash");
                    String roleText = result.getString("role");

                    Role role = Role.valueOf(roleText);

                    return new User(foundUsername, storedHash, role);
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find user", e);
        }

        return null;
    }
}