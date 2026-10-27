package com.smartfood.database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final String url;

    public DatabaseManager() {
        this("jdbc:sqlite:data/smartfood.db");
    }

    public DatabaseManager(String url) {
        this.url = url;
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            connection.close();
            throw e;
        }

        return connection;
    }
}
