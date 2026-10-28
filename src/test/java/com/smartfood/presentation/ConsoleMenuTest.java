package com.smartfood.presentation;

import com.smartfood.domain.MenuItem;
import com.smartfood.domain.Restaurant;
import com.smartfood.domain.Role;
import com.smartfood.domain.User;

import com.smartfood.repository.InMemoryRestaurantRepository;
import com.smartfood.repository.InMemoryUserRepository;
import com.smartfood.repository.RestaurantRepository;
import com.smartfood.repository.UserRepository;

import com.smartfood.security.PasswordHasher;
import com.smartfood.service.AuthService;
import com.smartfood.service.RestaurantService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleMenuTest {

    private AuthService authService;
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {

        // Create password hasher
        PasswordHasher hasher = new PasswordHasher();

        // Create test users
        UserRepository userRepository = new InMemoryUserRepository();

        userRepository.save(new User(
                "lana",
                hasher.hash("1234"),
                Role.CUSTOMER
        ));

        userRepository.save(new User(
                "admin",
                hasher.hash("admin123"),
                Role.MANAGER
        ));

        authService = new AuthService(userRepository, hasher);

        // Create test restaurants
        RestaurantRepository restaurantRepository =
                new InMemoryRestaurantRepository();

        Restaurant pizzaHouse = new Restaurant(
                "Pizza House",
                true,
                "Nablus",
                30.0
        );

        pizzaHouse.addMenuItem(
                new MenuItem("Margherita", 25.0, true)
        );

        pizzaHouse.addMenuItem(
                new MenuItem("Pepperoni", 30.0, false)
        );

        Restaurant burgerStation = new Restaurant(
                "Burger Station",
                true,
                "Nablus",
                20.0
        );

        burgerStation.addMenuItem(
                new MenuItem("Classic Burger", 30.0, true)
        );

        Restaurant closedCafe = new Restaurant(
                "Closed Cafe",
                false,
                "Nablus",
                10.0
        );

        closedCafe.addMenuItem(
                new MenuItem("Coffee", 12.0, true)
        );

        restaurantRepository.save(pizzaHouse);
        restaurantRepository.save(burgerStation);
        restaurantRepository.save(closedCafe);

        restaurantService = new RestaurantService(restaurantRepository);
    }

    /**
     * Runs the console using simulated keyboard input
     * and returns its printed output.
     */
    private String runConsole(String simulatedInput) {
        return runConsoleWithService(simulatedInput, restaurantService);
    }

    private String runConsoleWithService(
            String simulatedInput,
            RestaurantService service
    ) {

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        PrintStream originalOut = System.out;

        try (
                Scanner scanner = new Scanner(simulatedInput);
                PrintStream testOut = new PrintStream(
                        outputStream,
                        true,
                        StandardCharsets.UTF_8
                )
        ) {

            System.setOut(testOut);

            ConsoleMenu console = new ConsoleMenu(
                    authService,
                    service,
                    scanner
            );

            console.start();

        } finally {
            System.setOut(originalOut);
        }

        return outputStream.toString(StandardCharsets.UTF_8);
    }

    // ==========================================
    // TEST 1: WELCOME SCREEN
    // ==========================================

    @Test
    void welcomeMenuIsDisplayed() {

        String output = runConsole("0\n");

        assertTrue(
                output.contains("SMART FOOD DELIVERY SYSTEM")
        );

        assertTrue(
                output.contains("MAIN MENU")
        );

        assertTrue(
                output.contains("Login")
        );

        assertTrue(
                output.contains("Exit")
        );
    }

    // ==========================================
    // TEST 2: CUSTOMER LOGIN
    // ==========================================

    @Test
    void customerLoginSuccessfully() {

        String input = """
                1
                lana
                1234
                3
                0
                """;

        String output = runConsole(input);

        assertTrue(
                output.contains("Login successful!")
        );

        assertTrue(
                output.contains("Welcome, lana!")
        );

        assertTrue(
                output.contains("CUSTOMER DASHBOARD")
        );

        assertTrue(
                output.contains("Logged in as: lana")
        );
    }

    // ==========================================
    // TEST 3: MANAGER LOGIN
    // ==========================================

    @Test
    void managerLoginSuccessfully() {

        String input = """
                1
                admin
                admin123
                3
                0
                """;

        String output = runConsole(input);

        assertTrue(
                output.contains("Login successful!")
        );

        assertTrue(
                output.contains("MANAGER DASHBOARD")
        );

        assertTrue(
                output.contains("Role: MANAGER")
        );
    }

    // ==========================================
    // TEST 4: WRONG PASSWORD
    // ==========================================

    @Test
    void wrongPasswordIsRejected() {

        String input = """
                1
                lana
                wrong
                0
                """;

        String output = runConsole(input);

        assertTrue(
                output.contains(
                        "Login failed: Invalid username or password."
                )
        );

        assertFalse(
                output.contains("CUSTOMER DASHBOARD")
        );
    }

    // ==========================================
    // TEST 5: UNKNOWN USER
    // ==========================================

    @Test
    void unknownUserIsRejected() {

        String input = """
                1
                unknown
                password
                0
                """;

        String output = runConsole(input);

        assertTrue(
                output.contains(
                        "Login failed: Invalid username or password."
                )
        );
    }

    // ==========================================
    // TEST 6: OPEN RESTAURANTS ONLY
    // ==========================================

    @Test
    void customerSeesOnlyOpenRestaurants() {

        String input = """
                1
                lana
                1234
                1
                3
                0
                """;

        String output = runConsole(input);

        assertTrue(
                output.contains("Pizza House")
        );

        assertTrue(
                output.contains("Burger Station")
        );

        assertFalse(
                output.contains("Closed Cafe")
        );
    }

    // ==========================================
    // TEST 7: AVAILABLE MENU ITEMS ONLY
    // ==========================================

    @Test
    void customerSeesOnlyAvailableMenuItems() {

        String input = """
                1
                lana
                1234
                2
                1
                3
                0
                """;

        String output = runConsole(input);

        assertTrue(
                output.contains("Pizza House")
        );

        assertTrue(
                output.contains("Margherita")
        );

        assertTrue(
                output.contains("25.00")
        );

        assertFalse(
                output.contains("Pepperoni")
        );
    }

    // ==========================================
    // TEST 8: INVALID MAIN MENU CHOICE
    // ==========================================

    @Test
    void invalidMenuChoiceIsHandled() {

        String input = """
                99
                0
                """;

        String output = runConsole(input);

        assertTrue(
                output.contains(
                        "Invalid choice. Please try again."
                )
        );

        assertTrue(
                output.contains(
                        "Thank you for using Smart Food Delivery!"
                )
        );
    }

    // ==========================================
    // TEST 9: NON-NUMERIC INPUT
    // ==========================================

    @Test
    void nonNumericInputIsHandled() {

        String input = """
                abc
                0
                """;

        String output = runConsole(input);

        assertTrue(
                output.contains(
                        "Invalid input. Please enter a number."
                )
        );
    }

    // ==========================================
    // TEST 10: LOGOUT
    // ==========================================

    @Test
    void logoutReturnsToWelcomeMenu() {

        String input = """
                1
                lana
                1234
                3
                0
                """;

        String output = runConsole(input);

        assertTrue(
                output.contains("Logged out successfully.")
        );

        // Welcome menu should appear twice:
        // before login and after logout.
        assertEquals(
                2,
                output.split("MAIN MENU", -1).length - 1
        );
    }

    // ==========================================
    // TEST 11: EXIT
    // ==========================================

    @Test
    void exitTerminatesApplication() {

        String output = runConsole("0\n");

        assertTrue(
                output.contains(
                        "Thank you for using Smart Food Delivery!"
                )
        );
    }

    // ==========================================
    // TEST 12: INVALID RESTAURANT NUMBER
    // ==========================================

    @Test
    void invalidRestaurantSelectionIsHandled() {

        String input = """
                1
                lana
                1234
                2
                99
                3
                0
                """;

        String output = runConsole(input);

        assertTrue(
                output.contains("Invalid restaurant number.")
        );
    }

    @Test
    void managerCanBrowseRestaurantsAndMenus() {

        String input = """
            1
            admin
            admin123
            1
            2
            1
            3
            0
            """;

        String output = runConsole(input);

        assertTrue(output.contains("MANAGER DASHBOARD"));
        assertTrue(output.contains("Pizza House"));
        assertTrue(output.contains("Margherita"));
    }
    @Test
    void managerInvalidChoiceIsHandled() {

        String input = """
            1
            admin
            admin123
            99
            3
            0
            """;

        String output = runConsole(input);

        assertTrue(
                output.contains("Invalid choice. Please try again.")
        );

        assertTrue(
                output.contains("Logged out successfully.")
        );
    }

    @Test
    void customerInvalidChoiceIsHandled() {

        String input = """
            1
            lana
            1234
            99
            3
            0
            """;

        String output = runConsole(input);

        assertTrue(
                output.contains("Invalid choice. Please try again.")
        );

        assertTrue(
                output.contains("Logged out successfully.")
        );
    }

    @Test
    void customerCanExitDirectly() {

        String input = """
            1
            lana
            1234
            0
            """;

        String output = runConsole(input);

        assertTrue(output.contains("CUSTOMER DASHBOARD"));

        assertTrue(
                output.contains("Thank you for using Smart Food Delivery!")
        );
    }
    void emptyInputExitsGracefully() {

        String output = runConsole("");

        assertTrue(
                output.contains("Thank you for using Smart Food Delivery!")
        );
    }
    @Test
    void customerCanReturnFromRestaurantSelection() {

        String input = """
            1
            lana
            1234
            2
            0
            3
            0
            """;

        String output = runConsole(input);

        assertTrue(output.contains("CUSTOMER DASHBOARD"));

        assertTrue(output.contains("Logged out successfully."));
    }

    @Test
    void noOpenRestaurantsMessageIsDisplayed() {

        RestaurantRepository emptyRepository =
                new InMemoryRestaurantRepository();

        RestaurantService emptyService =
                new RestaurantService(emptyRepository);

        String input = """
            1
            lana
            1234
            1
            3
            0
            """;

        String output = runConsoleWithService(input, emptyService);

        assertTrue(
                output.contains("No open restaurants available.")
        );
    }
}