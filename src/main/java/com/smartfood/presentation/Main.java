package com.smartfood.presentation;

import com.smartfood.domain.*;
import com.smartfood.repository.*;
import com.smartfood.service.*;

public class Main {
    public static void main(String[] args) {
        UserRepository users = new UserRepository();
        users.save(new User("lana", "1234", Role.CUSTOMER));
        users.save(new User("admin", "admin", Role.MANAGER));

        RestaurantRepository repo = new RestaurantRepository();
        Restaurant pizza = new Restaurant("Pizza House", true);
        pizza.addMenuItem(new MenuItem("Margherita", 25.0, true));
        pizza.addMenuItem(new MenuItem("Pepperoni", 30.0, false));
        Restaurant closed = new Restaurant("Closed Cafe", false);
        repo.save(pizza);
        repo.save(closed);

        AuthService auth = new AuthService(users);
        RestaurantService rs = new RestaurantService(repo);

        User u = auth.login("lana", "1234");
        System.out.println("Logged in as: " + u.getUsername() + " (" + u.getRole() + ")");

        rs.getOpenRestaurants().forEach(r -> System.out.println("Open: " + r.getName()));
        rs.getAvailableMenu("Pizza House").forEach(
                i -> System.out.println(i.getName() + " - " + i.getPrice()));
    }
}