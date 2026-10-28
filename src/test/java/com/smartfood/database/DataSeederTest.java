package com.smartfood.database;

import com.smartfood.repository.RestaurantRepository;
import com.smartfood.repository.SqliteRestaurantRepository;
import com.smartfood.repository.SqliteUserRepository;
import com.smartfood.repository.UserRepository;

import com.smartfood.security.PasswordHasher;
import com.smartfood.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DataSeederTest {

    @TempDir
    Path tempDir;

    private UserRepository userRepository;
    private RestaurantRepository restaurantRepository;
    private DataSeeder seeder;
    private PasswordHasher hasher;

    @BeforeEach
    void setUp() throws SQLException {

        Path databasePath = tempDir.resolve("seed-test.db");

        DatabaseManager databaseManager = new DatabaseManager(
                "jdbc:sqlite:" + databasePath.toAbsolutePath()
        );

        new DatabaseInitializer(databaseManager).initialize();

        userRepository = new SqliteUserRepository(databaseManager);

        restaurantRepository =
                new SqliteRestaurantRepository(databaseManager);

        hasher = new PasswordHasher();

        seeder = new DataSeeder(
                userRepository,
                restaurantRepository,
                hasher
        );
    }

    @Test
    void seedCreatesUsers() {

        seeder.seed();

        assertNotNull(
                userRepository.findByUsername("lana")
        );

        assertNotNull(
                userRepository.findByUsername("admin")
        );
    }

    @Test
    void seededCustomerCanLogin() {

        seeder.seed();

        AuthService auth = new AuthService(
                userRepository,
                hasher
        );

        assertEquals(
                "lana",
                auth.login("lana", "1234").getUsername()
        );
    }

    @Test
    void seedCreatesRestaurants() {

        seeder.seed();

        assertEquals(
                3,
                restaurantRepository.findAll().size()
        );

        assertNotNull(
                restaurantRepository.findByName("Pizza House")
        );
    }

    @Test
    void seedDoesNotDuplicateData() {

        seeder.seed();
        seeder.seed();

        assertEquals(
                3,
                restaurantRepository.findAll().size()
        );

        assertNotNull(
                userRepository.findByUsername("lana")
        );

        assertNotNull(
                userRepository.findByUsername("admin")
        );
    }
}