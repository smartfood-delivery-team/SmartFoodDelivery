package com.smartfood.repository;

import com.smartfood.database.DatabaseInitializer;
import com.smartfood.database.DatabaseManager;
import com.smartfood.domain.MenuItem;
import com.smartfood.domain.Restaurant;
import com.smartfood.service.RestaurantService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqliteRestaurantRepositoryTest {

    @TempDir
    Path tempDir;

    private DatabaseManager databaseManager;
    private RestaurantRepository repository;

    @BeforeEach
    void setUp() throws SQLException {

        Path databasePath = tempDir.resolve("restaurants-test.db");

        databaseManager = new DatabaseManager(
                "jdbc:sqlite:" + databasePath.toAbsolutePath()
        );

        new DatabaseInitializer(databaseManager).initialize();

        repository = new SqliteRestaurantRepository(databaseManager);
    }

    // TEST 1: Save restaurant
    @Test
    void saveRestaurantSuccessfully() {

        Restaurant restaurant = new Restaurant(
                "Pizza House",
                true,
                "Nablus",
                30.0
        );

        repository.save(restaurant);

        Restaurant saved = repository.findByName("Pizza House");

        assertNotNull(saved);
        assertEquals("Pizza House", saved.getName());
        assertTrue(saved.isOpen());
        assertEquals("Nablus", saved.getDeliveryZone());
        assertEquals(30.0, saved.getMinimumOrder());
    }

    // TEST 2: Save restaurant with menu items
    @Test
    void saveRestaurantWithMenuItems() {

        Restaurant restaurant = new Restaurant(
                "Pizza House",
                true,
                "Nablus",
                30.0
        );

        restaurant.addMenuItem(
                new MenuItem("Margherita", 25.0, true)
        );

        restaurant.addMenuItem(
                new MenuItem("Pepperoni", 30.0, false)
        );

        repository.save(restaurant);

        Restaurant saved = repository.findByName("Pizza House");

        assertNotNull(saved);
        assertEquals(2, saved.getMenu().size());

        assertEquals("Margherita", saved.getMenu().get(0).getName());
        assertEquals(25.0, saved.getMenu().get(0).getPrice());
        assertTrue(saved.getMenu().get(0).isAvailable());

        assertEquals("Pepperoni", saved.getMenu().get(1).getName());
        assertFalse(saved.getMenu().get(1).isAvailable());
    }

    // TEST 3: Retrieve all restaurants
    @Test
    void findAllRestaurants() {

        Restaurant pizza = new Restaurant(
                "Pizza House",
                true,
                "Nablus",
                30.0
        );

        Restaurant burger = new Restaurant(
                "Burger Station",
                false,
                "Ramallah",
                20.0
        );

        repository.save(pizza);
        repository.save(burger);

        List<Restaurant> restaurants = repository.findAll();

        assertEquals(2, restaurants.size());

        assertTrue(
                restaurants.stream().anyMatch(
                        r -> r.getName().equals("Pizza House")
                )
        );

        assertTrue(
                restaurants.stream().anyMatch(
                        r -> r.getName().equals("Burger Station")
                )
        );
    }

    // TEST 4: Unknown restaurant
    @Test
    void findUnknownRestaurantReturnsNull() {

        Restaurant restaurant =
                repository.findByName("Unknown Restaurant");

        assertNull(restaurant);
    }

    // TEST 5: Find restaurant ignoring case
    @Test
    void findRestaurantIgnoringCase() {

        Restaurant restaurant = new Restaurant(
                "Pizza House",
                true,
                "Nablus",
                30.0
        );

        repository.save(restaurant);

        Restaurant found = repository.findByName("pizza house");

        assertNotNull(found);
        assertEquals("Pizza House", found.getName());
    }

    // TEST 6: Check SQLite persistence
    @Test
    void dataPersistsAfterReconnect() {

        Restaurant restaurant = new Restaurant(
                "Pizza House",
                true,
                "Nablus",
                30.0
        );

        restaurant.addMenuItem(
                new MenuItem("Margherita", 25.0, true)
        );

        repository.save(restaurant);

        DatabaseManager secondManager = new DatabaseManager(
                "jdbc:sqlite:" +
                        tempDir.resolve("restaurants-test.db").toAbsolutePath()
        );

        RestaurantRepository secondRepository =
                new SqliteRestaurantRepository(secondManager);

        Restaurant saved =
                secondRepository.findByName("Pizza House");

        assertNotNull(saved);
        assertEquals("Pizza House", saved.getName());
        assertEquals(1, saved.getMenu().size());
        assertEquals("Margherita", saved.getMenu().get(0).getName());
    }

    // TEST 7: Transaction rollback
    @Test
    void invalidMenuPriceRollsBackRestaurant() {

        Restaurant restaurant = new Restaurant(
                "Invalid Restaurant",
                true,
                "Nablus",
                30.0
        );

        restaurant.addMenuItem(
                new MenuItem("Valid Item", 25.0, true)
        );

        restaurant.addMenuItem(
                new MenuItem("Invalid Item", -10.0, true)
        );

        assertThrows(
                IllegalStateException.class,
                () -> repository.save(restaurant)
        );

        Restaurant saved =
                repository.findByName("Invalid Restaurant");

        assertNull(saved);
    }

    // TEST 8: Open restaurants only
    @Test
    void onlyOpenRestaurantsAreDisplayed() {

        Restaurant openRestaurant = new Restaurant(
                "Pizza House",
                true,
                "Nablus",
                30.0
        );

        Restaurant closedRestaurant = new Restaurant(
                "Closed Cafe",
                false,
                "Nablus",
                15.0
        );

        repository.save(openRestaurant);
        repository.save(closedRestaurant);

        RestaurantService service = new RestaurantService(repository);

        List<Restaurant> openRestaurants =
                service.getOpenRestaurants();

        assertEquals(1, openRestaurants.size());
        assertEquals("Pizza House", openRestaurants.get(0).getName());
    }

    // TEST 9: Available menu items only
    @Test
    void onlyAvailableMenuItemsAreDisplayed() {

        Restaurant restaurant = new Restaurant(
                "Pizza House",
                true,
                "Nablus",
                30.0
        );

        restaurant.addMenuItem(
                new MenuItem("Margherita", 25.0, true)
        );

        restaurant.addMenuItem(
                new MenuItem("Pepperoni", 30.0, false)
        );

        repository.save(restaurant);

        RestaurantService service = new RestaurantService(repository);

        List<MenuItem> availableMenu =
                service.getAvailableMenu("Pizza House");

        assertEquals(1, availableMenu.size());
        assertEquals("Margherita", availableMenu.get(0).getName());
    }
}