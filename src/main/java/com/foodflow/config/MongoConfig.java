package com.foodflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {
    // This class configures MongoDB auditing to automatically populate createdAt and updatedAt dates.
}
