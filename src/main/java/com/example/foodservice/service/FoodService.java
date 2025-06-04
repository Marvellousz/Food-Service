package com.example.foodservice.service;

import com.example.foodservice.model.Food;
import java.util.List;

public interface FoodService {
    /**
     * Get all food items
     * @return List of all food items
     */
    List<Food> getAllFoodItems();

    /**
     * Get food item by id
     * @param id Food item id
     * @return Food item with the given id
     */
    Food getFoodItemById(Integer id);

    /**
     * Search food items by name (case-insensitive partial match)
     * @param name Food item name to search
     * @return List of food items matching the given name
     */
    List<Food> searchFoodItemsByName(String name);
}