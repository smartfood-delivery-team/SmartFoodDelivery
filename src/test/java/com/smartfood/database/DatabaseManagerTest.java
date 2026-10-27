package com.smartfood.database;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseManagerTest {
    @Test
    void conntectionTesting() throws SQLException {
        DatabaseManager db = new DatabaseManager();
        try (Connection connection = db.getConnection()){
            assertNotNull(connection);

            assertFalse(connection.isClosed());
        }
    }

}
