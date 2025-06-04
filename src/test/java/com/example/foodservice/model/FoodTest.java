package com.example.foodservice.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FoodTest {

    @Test
    void testFoodBuilderAndGetters() {
        // When
        Food food = Food.builder()
                .id(1)
                .name("Palak paneer")
                .price("$5.95")
                .description("Fresh spinach leaves (palak) cooked with cubes of Paneer cheese")
                .calories(650)
                .build();

        // Then
        assertEquals(1, food.getId());
        assertEquals("Palak paneer", food.getName());
        assertEquals("$5.95", food.getPrice());
        assertEquals("Fresh spinach leaves (palak) cooked with cubes of Paneer cheese", food.getDescription());
        assertEquals(650, food.getCalories());
    }

    @Test
    void testFoodAllArgsConstructor() {
        // When
        Food food = new Food(2, "Biryani", "$7.95", "A fragrant and flavorful Indian rice dish", 900);

        // Then
        assertEquals(2, food.getId());
        assertEquals("Biryani", food.getName());
        assertEquals("$7.95", food.getPrice());
        assertEquals("A fragrant and flavorful Indian rice dish", food.getDescription());
        assertEquals(900, food.getCalories());
    }

    @Test
    void testFoodNoArgsConstructorAndSetters() {
        // Given
        Food food = new Food();

        // When
        food.setId(3);
        food.setName("Masala Dosa");
        food.setPrice("$6.50");
        food.setDescription("A South Indian crepe made from fermented rice batter and black lentils");
        food.setCalories(450);

        // Then
        assertEquals(3, food.getId());
        assertEquals("Masala Dosa", food.getName());
        assertEquals("$6.50", food.getPrice());
        assertEquals("A South Indian crepe made from fermented rice batter and black lentils", food.getDescription());
        assertEquals(450, food.getCalories());
    }

    @Test
    void testFoodEqualsAndHashCode() {
        // Given
        Food food1 = new Food(1, "Palak paneer", "$5.95", "Description 1", 650);
        Food food2 = new Food(1, "Palak paneer", "$5.95", "Description 1", 650);
        Food food3 = new Food(2, "Biryani", "$7.95", "Description 2", 900);

        // Then
        assertEquals(food1, food2);
        assertEquals(food1.hashCode(), food2.hashCode());
        assertNotEquals(food1, food3);
        assertNotEquals(food1.hashCode(), food3.hashCode());
    }

    @Test
    void testFoodToString() {
        // Given
        Food food = new Food(1, "Palak paneer", "$5.95", "Description", 650);

        // When
        String toStringResult = food.toString();

        // Then
        assertTrue(toStringResult.contains("Food"));
        assertTrue(toStringResult.contains("id=1"));
        assertTrue(toStringResult.contains("name=Palak paneer"));
        assertTrue(toStringResult.contains("price=$5.95"));
        assertTrue(toStringResult.contains("description=Description"));
        assertTrue(toStringResult.contains("calories=650"));
    }
    
    @Test
    void testFoodBuilderToString() {
        // When
        String builderString = Food.builder().toString();
        
        // Then
        assertTrue(builderString.contains("Food.FoodBuilder"));
    }
}
