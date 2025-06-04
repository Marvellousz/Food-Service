package com.example.foodservice.model;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FoodMenuTest {

    @Test
    void testFoodMenuConstructorAndGetters() {
        // Given
        Food food1 = Food.builder()
                .id(1)
                .name("Palak paneer")
                .price("$5.95")
                .description("Fresh spinach leaves (palak) cooked with cubes of Paneer cheese")
                .calories(650)
                .build();

        Food food2 = Food.builder()
                .id(2)
                .name("Biryani")
                .price("$7.95")
                .description("A fragrant and flavorful Indian rice dish")
                .calories(900)
                .build();

        List<Food> foodList = Arrays.asList(food1, food2);

        // When
        FoodMenu foodMenu = new FoodMenu(foodList);

        // Then
        assertEquals(foodList, foodMenu.getFoodList());
        assertEquals(2, foodMenu.getFoodList().size());
        assertEquals(1, foodMenu.getFoodList().get(0).getId());
        assertEquals(2, foodMenu.getFoodList().get(1).getId());
    }

    @Test
    void testFoodMenuNoArgsConstructorAndSetter() {
        // Given
        Food food1 = Food.builder()
                .id(1)
                .name("Palak paneer")
                .build();

        List<Food> foodList = Arrays.asList(food1);

        // When
        FoodMenu foodMenu = new FoodMenu();
        foodMenu.setFoodList(foodList);

        // Then
        assertEquals(foodList, foodMenu.getFoodList());
        assertEquals(1, foodMenu.getFoodList().size());
    }

    @Test
    void testFoodMenuEqualsAndHashCode() {
        // Given
        Food food1 = Food.builder()
                .id(1)
                .name("Test Food")
                .build();

        List<Food> foodList1 = Arrays.asList(food1);
        List<Food> foodList2 = Arrays.asList(food1);

        FoodMenu foodMenu1 = new FoodMenu(foodList1);
        FoodMenu foodMenu2 = new FoodMenu(foodList2);
        FoodMenu foodMenu3 = new FoodMenu(Arrays.asList(Food.builder().id(2).name("Different").build()));

        // Then
        assertEquals(foodMenu1, foodMenu2);
        assertEquals(foodMenu1.hashCode(), foodMenu2.hashCode());
        assertNotEquals(foodMenu1, foodMenu3);
        assertNotEquals(foodMenu1.hashCode(), foodMenu3.hashCode());
    }

    @Test
    void testFoodMenuToString() {
        // Given
        Food food1 = Food.builder()
                .id(1)
                .name("Test Food")
                .build();

        List<Food> foodList = Arrays.asList(food1);
        FoodMenu foodMenu = new FoodMenu(foodList);

        // When
        String toStringResult = foodMenu.toString();

        // Then
        assertTrue(toStringResult.contains("FoodMenu"));
        assertTrue(toStringResult.contains("foodList="));
    }
}
