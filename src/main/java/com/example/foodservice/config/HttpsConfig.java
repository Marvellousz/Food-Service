package com.example.foodservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("ssl")
public class HttpsConfig {
    // SSL configuration is handled by application-ssl.yml
    // This class exists to enable the ssl profile
}