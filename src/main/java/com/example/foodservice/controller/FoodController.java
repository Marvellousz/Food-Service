package com.example.foodservice.controller;

import com.example.foodservice.model.Food;
import com.example.foodservice.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @GetMapping
    public ResponseEntity<List<Food>> getAllFoodItems() {
        List<Food> foods = foodService.getAllFoodItems();
        return ResponseEntity.ok(foods);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Food> getFoodItemById(@PathVariable Integer id) {
        Food food = foodService.getFoodItemById(id);
        return ResponseEntity.ok(food);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Food>> searchFoodItemsByName(@RequestParam String name) {
        List<Food> foods = foodService.searchFoodItemsByName(name);
        return ResponseEntity.ok(foods);
    }
}