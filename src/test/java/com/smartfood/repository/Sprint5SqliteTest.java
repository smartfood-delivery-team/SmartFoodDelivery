package com.smartfood.repository;

import com.smartfood.database.DatabaseInitializer;
import com.smartfood.database.DatabaseManager;
import com.smartfood.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class Sprint5SqliteTest {
    @TempDir Path dir;
    private void seed(DatabaseManager manager) throws Exception {
        try (Connection c=manager.getConnection(); Statement s=c.createStatement()) {
            s.executeUpdate("INSERT INTO users(username,password_hash,role) VALUES ('lana','hash','CUSTOMER')");
            s.executeUpdate("INSERT INTO restaurants(id,name,is_open,delivery_zone,minimum_order) VALUES (1,'Pizza House',1,'Nablus',0)");
            s.executeUpdate("INSERT INTO menu_items(restaurant_id,name,price,available) VALUES (1,'Pizza',20,1)");
        }
    }
    @Test void orderAndDriverTypesSurviveReconnection() throws Exception {
        String url="jdbc:sqlite:"+dir.resolve("s5.db").toAbsolutePath();
        DatabaseManager manager=new DatabaseManager(url);
        new DatabaseInitializer(manager).initialize(); seed(manager);
        SqliteDriverRepository drivers=new SqliteDriverRepository(manager);
        drivers.save(new ElectricVehicleDriver("EV1","Sara",true,2,7,75));
        LocalDateTime scheduled=LocalDateTime.now().plusDays(2).withNano(0);
        Order order=new ScheduledOrder("S5",new User("lana","hash",Role.CUSTOMER),
                new Restaurant("Pizza House",true,"Nablus",0),
                List.of(new OrderItem(new MenuItem("Pizza",20,true),2)),"Nablus",scheduled);
        order.calculateDeliveryFee(10);
        new SqliteOrderRepository(manager).save(order);
        DatabaseManager reopened=new DatabaseManager(url);
        Driver driver=new SqliteDriverRepository(reopened).findById("EV1");
        assertInstanceOf(ElectricVehicleDriver.class,driver);
        assertEquals(75,driver.getRemainingRangeKm(),0.001);
        Order loaded=new SqliteOrderRepository(reopened).findById("S5");
        assertInstanceOf(ScheduledOrder.class,loaded);
        assertEquals(scheduled,((ScheduledOrder)loaded).getScheduledFor());
        assertEquals(10,loaded.getDeliveryDistanceKm(),0.001);
    }
    @Test void expressSubtypeAndDriverReferenceSurviveRestart() throws Exception {
        String url="jdbc:sqlite:"+dir.resolve("s5-express.db").toAbsolutePath();
        DatabaseManager manager=new DatabaseManager(url);
        new DatabaseInitializer(manager).initialize(); seed(manager);
        SqliteDriverRepository drivers=new SqliteDriverRepository(manager);
        drivers.save(new CarDriver("C1","Khaled",true,1,3));
        Order order=new ExpressOrder("EX1",new User("lana","hash",Role.CUSTOMER),
                new Restaurant("Pizza House",true,"Nablus",0),
                List.of(new OrderItem(new MenuItem("Pizza",20,true),1)),"Nablus");
        order.calculateDeliveryFee(2);
        order.assignDriver(drivers.findById("C1"));
        drivers.save(order.getAssignedDriver());
        new SqliteOrderRepository(manager).save(order);
        Order loaded=new SqliteOrderRepository(new DatabaseManager(url)).findById("EX1");
        assertInstanceOf(ExpressOrder.class,loaded);
        assertInstanceOf(CarDriver.class,loaded.getAssignedDriver());
        assertEquals(5+2*2+20+5,loaded.getFinalTotal(),0.001);
    }
}
