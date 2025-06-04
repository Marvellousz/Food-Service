package com.example.foodservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class ApplicationConfig {

    @Value("${foodservice.data.file.path}")
    private String foodDataFilePath;
}