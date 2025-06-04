package com.example.foodservice.controller;

import com.example.foodservice.controller.FoodController;
import com.example.foodservice.exception.FoodNotFoundException;
import com.example.foodservice.model.Food;
import com.example.foodservice.service.FoodService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FoodController.class)
public class FoodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FoodService foodService;

    private List<Food> foodItems;
    private Food food1;
    private Food food2;

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

        foodItems = Arrays.asList(food1, food2);
    }

    @Test
    void getAllFoodItems_shouldReturnAllFoodItems() throws Exception {
        when(foodService.getAllFoodItems()).thenReturn(foodItems);

        mockMvc.perform(get("/api/foods")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Palak paneer")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Biryani")));

        verify(foodService, times(1)).getAllFoodItems();
    }

    @Test
    void getFoodItemById_withValidId_shouldReturnFoodItem() throws Exception {
        when(foodService.getFoodItemById(1)).thenReturn(food1);

        mockMvc.perform(get("/api/foods/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Palak paneer")))
                .andExpect(jsonPath("$.price", is("$5.95")))
                .andExpect(jsonPath("$.calories", is(650)));

        verify(foodService, times(1)).getFoodItemById(1);
    }

    @Test
    void getFoodItemById_withInvalidId_shouldReturn404() throws Exception {
        when(foodService.getFoodItemById(99)).thenThrow(new FoodNotFoundException("Food item not found with id: 99"));

        mockMvc.perform(get("/api/foods/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Food item not found with id: 99")));

        verify(foodService, times(1)).getFoodItemById(99);
    }

    @Test
    void searchFoodItemsByName_withValidName_shouldReturnMatchingFoodItems() throws Exception {
        when(foodService.searchFoodItemsByName("pan")).thenReturn(Collections.singletonList(food1));

        mockMvc.perform(get("/api/foods/search")
                        .param("name", "pan")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Palak paneer")));

        verify(foodService, times(1)).searchFoodItemsByName("pan");
    }

    @Test
    void searchFoodItemsByName_withNoMatches_shouldReturnEmptyList() throws Exception {
        when(foodService.searchFoodItemsByName("xyz")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/foods/search")
                        .param("name", "xyz")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(foodService, times(1)).searchFoodItemsByName("xyz");
    }
}