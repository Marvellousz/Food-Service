package com.example.foodservice.service;

import com.example.foodservice.config.ApplicationConfig;
import com.example.foodservice.exception.FoodNotFoundException;
import com.example.foodservice.model.Food;
import com.example.foodservice.model.FoodMenu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FoodServiceImplTest {

    @Mock
    private ApplicationConfig applicationConfig;

    @Spy
    @InjectMocks
    private FoodServiceImpl foodService;

    private Food food1;
    private Food food2;
    private FoodMenu foodMenu;

    @BeforeEach
    void setUp() {
        food1 = Food.builder()
                .id(1)
                .name("Palak paneer")
                .price("$5.95")
                .description("Fresh spinach leaves (palak) cooked with cubes of Paneer cheese")
                .calories(650)
                .build();

        food2 = Food.builder()
                .id(2)
                .name("Biryani")
                .price("$7.95")
                .description("A fragrant and flavorful Indian rice dish")
                .calories(900)
                .build();

        foodMenu = new FoodMenu();
        foodMenu.setFoodList(Arrays.asList(food1, food2));

        // Mock the XML loading by directly setting the foodMenu
        ReflectionTestUtils.setField(foodService, "foodMenu", foodMenu);
    }

    @Test
    void getAllFoodItems_shouldReturnAllFoodItems() {
        // When
        List<Food> result = foodService.getAllFoodItems();

        // Then
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Palak paneer", result.get(0).getName());
        assertEquals(2, result.get(1).getId());
        assertEquals("Biryani", result.get(1).getName());
    }

    @Test
    void getFoodItemById_withValidId_shouldReturnFoodItem() {
        // When
        Food result = foodService.getFoodItemById(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Palak paneer", result.getName());
        assertEquals("$5.95", result.getPrice());
        assertEquals(650, result.getCalories());
    }

    @Test
    void getFoodItemById_withInvalidId_shouldThrowException() {
        // When & Then
        FoodNotFoundException exception = assertThrows(FoodNotFoundException.class,
                () -> foodService.getFoodItemById(99));

        assertEquals("Food item not found with id: 99", exception.getMessage());
    }

    @Test
    void searchFoodItemsByName_withValidPartialName_shouldReturnMatchingItems() {
        // When
        List<Food> result = foodService.searchFoodItemsByName("Pan");

        // Then
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Palak paneer", result.get(0).getName());
    }

    @Test
    void searchFoodItemsByName_withExactName_shouldReturnMatchingItem() {
        // When
        List<Food> result = foodService.searchFoodItemsByName("Biryani");

        // Then
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getId());
        assertEquals("Biryani", result.get(0).getName());
    }

    @Test
    void searchFoodItemsByName_withNoMatches_shouldReturnEmptyList() {
        // When
        List<Food> result = foodService.searchFoodItemsByName("Pizza");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void searchFoodItemsByName_withNullName_shouldReturnEmptyList() {
        // When
        List<Food> result = foodService.searchFoodItemsByName(null);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void searchFoodItemsByName_withEmptyName_shouldReturnEmptyList() {
        // When
        List<Food> result = foodService.searchFoodItemsByName("  ");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void init_shouldLoadFoodDataFromXml() {
        // We need to stub the loadFoodDataFromXml method to avoid actual file loading
        doNothing().when(foodService).loadFoodDataFromXml();

        // When - actually call the init method
        foodService.init();

        // Then - verify that our methods were called in the expected order
        verify(foodService, times(1)).loadFoodDataFromXml();
    }

    @Test
    void loadFoodDataFromXml_withClasspathResource_shouldLoadData() {
        // Given
        String classpathLocation = "breakfast_menu.xml";
        when(applicationConfig.getFoodDataFilePath()).thenReturn("classpath:" + classpathLocation);

        // Reset the food menu to ensure we're testing the loading
        ReflectionTestUtils.setField(foodService, "foodMenu", null);

        // When
        foodService.loadFoodDataFromXml();

        // Then
        FoodMenu resultMenu = (FoodMenu) ReflectionTestUtils.getField(foodService, "foodMenu");
        assertNotNull(resultMenu);
        assertNotNull(resultMenu.getFoodList());
        assertFalse(resultMenu.getFoodList().isEmpty());
    }

    @Test
    void loadFoodDataFromXml_withFileNotFound_shouldCreateEmptyFoodMenu() {
        // Given
        when(applicationConfig.getFoodDataFilePath()).thenReturn("nonexistent-file.xml");

        // Reset the food menu to ensure we're testing the loading
        ReflectionTestUtils.setField(foodService, "foodMenu", null);

        // When
        foodService.loadFoodDataFromXml();

        // Then
        FoodMenu resultMenu = (FoodMenu) ReflectionTestUtils.getField(foodService, "foodMenu");
        assertNotNull(resultMenu);
        assertNotNull(resultMenu.getFoodList());
        assertTrue(resultMenu.getFoodList().isEmpty());
    }
}