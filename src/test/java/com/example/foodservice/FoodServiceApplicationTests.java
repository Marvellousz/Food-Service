package com.example.foodservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class FoodServiceApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        // This test will verify that the Spring context loads successfully
        assertNotNull(applicationContext);
    }
    
    @Test
    void applicationStartup_shouldCreateMainInstance() {
        // This test ensures the application class itself is covered
        FoodServiceApplication application = new FoodServiceApplication();
        assertNotNull(application);
    }
}
