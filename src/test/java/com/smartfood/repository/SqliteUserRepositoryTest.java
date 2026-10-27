package com.smartfood.repository;

import com.smartfood.database.DatabaseInitializer;
import com.smartfood.database.DatabaseManager;
import com.smartfood.domain.Role;
import com.smartfood.domain.User;
import com.smartfood.security.PasswordHasher;
import com.smartfood.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SqliteUserRepositoryTest {

    @TempDir
    Path tempDir;

    private DatabaseManager databaseManager;
    private UserRepository repository;
    private PasswordHasher hasher;

    @BeforeEach
    void setUp() throws Exception {

        Path databasePath = tempDir.resolve("users-test.db");

        databaseManager = new DatabaseManager(
                "jdbc:sqlite:" + databasePath.toAbsolutePath()
        );

        new DatabaseInitializer(databaseManager).initialize();

        repository = new SqliteUserRepository(databaseManager);

        hasher = new PasswordHasher();
    }
    @Test
    void saveUserSuccessfully() {

        User user = new User(
                "osama",
                hasher.hash("1234"),
                Role.CUSTOMER
        );

        repository.save(user);

        User savedUser = repository.findByUsername("osama");

        assertNotNull(savedUser);
        assertEquals("osama", savedUser.getUsername());
        assertEquals(Role.CUSTOMER, savedUser.getRole());
    }
    @Test
    void findUnknownUser() {

        User user = repository.findByUsername("nobody");

        assertNull(user);
    }
    @Test
    void duplicateUsernameRejected() {

        User first = new User(
                "osama",
                hasher.hash("1234"),
                Role.CUSTOMER
        );

        User second = new User(
                "osama",
                hasher.hash("5678"),
                Role.MANAGER
        );

        repository.save(first);

        assertThrows(
                IllegalStateException.class,
                () -> repository.save(second)
        );
    }
    @Test
    void dataPersistsAfterReconnect() {

        User user = new User(
                "osama",
                hasher.hash("1234"),
                Role.CUSTOMER
        );

        repository.save(user);

        DatabaseManager secondManager = new DatabaseManager(
                "jdbc:sqlite:" +
                        tempDir.resolve("users-test.db").toAbsolutePath()
        );

        UserRepository secondRepository =
                new SqliteUserRepository(secondManager);

        User savedUser = secondRepository.findByUsername("osama");

        assertNotNull(savedUser);
        assertEquals("osama", savedUser.getUsername());
    }
    @Test
    void loginWithSqliteRepository() {

        User user = new User(
                "osama",
                hasher.hash("1234"),
                Role.CUSTOMER
        );

        repository.save(user);

        AuthService auth = new AuthService(repository, hasher);

        User loggedInUser = auth.login("osama", "1234");

        assertNotNull(loggedInUser);
        assertEquals("osama", loggedInUser.getUsername());

        assertThrows(
                IllegalArgumentException.class,
                () -> auth.login("osama", "wrong")
        );
    }
    @Test
    void passwordIsStoredAsHash() {

        String originalPassword = "1234";

        User user = new User(
                "osama",
                hasher.hash(originalPassword),
                Role.CUSTOMER
        );

        repository.save(user);

        User savedUser = repository.findByUsername("osama");

        assertNotNull(savedUser);

        assertNotEquals(
                originalPassword,
                savedUser.getPasswordHash()
        );

        assertTrue(
                hasher.verify(
                        originalPassword,
                        savedUser.getPasswordHash()
                )
        );
    }

}