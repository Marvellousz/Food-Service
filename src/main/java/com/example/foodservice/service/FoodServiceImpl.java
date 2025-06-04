package com.example.foodservice.service;

import com.example.foodservice.config.ApplicationConfig;
import com.example.foodservice.exception.FoodNotFoundException;
import com.example.foodservice.model.Food;
import com.example.foodservice.model.FoodMenu;
import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodServiceImpl implements FoodService {

    private final ApplicationConfig applicationConfig;
    private FoodMenu foodMenu;

    @PostConstruct
    public void init() {
        loadFoodDataFromXml();
    }

    protected void loadFoodDataFromXml() {
        try {
            String path = applicationConfig.getFoodDataFilePath();
            Resource resource;

            // Handle classpath resources
            if (path.startsWith("classpath:")) {
                String classpathLocation = path.substring("classpath:".length());
                resource = new ClassPathResource(classpathLocation);
            } else {
                // Handle file system resources
                resource = new FileSystemResource(path);
            }

            if (!resource.exists()) {
                throw new IOException("Resource not found: " + path);
            }

            try (InputStream inputStream = resource.getInputStream()) {
                JAXBContext jaxbContext = JAXBContext.newInstance(FoodMenu.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                foodMenu = (FoodMenu) jaxbUnmarshaller.unmarshal(inputStream);
                log.info("Food data loaded successfully from {}", path);
            }
        } catch (JAXBException | IOException e) {
            log.error("Error loading food data from XML: {}", e.getMessage(), e);
            foodMenu = new FoodMenu();
            foodMenu.setFoodList(Collections.emptyList());
        }
    }

    @Override
    public List<Food> getAllFoodItems() {
        return foodMenu.getFoodList();
    }

    @Override
    public Food getFoodItemById(Integer id) {
        return foodMenu.getFoodList().stream()
                .filter(food -> food.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new FoodNotFoundException("Food item not found with id: " + id));
    }

    @Override
    public List<Food> searchFoodItemsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String searchTerm = name.toLowerCase().trim();

        return foodMenu.getFoodList().stream()
                .filter(food -> food.getName() != null &&
                        food.getName().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }
}