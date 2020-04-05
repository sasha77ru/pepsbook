package ru.sasha77.spring.pepsbook.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = "ru.sasha77.spring.pepsbook.repositories")
@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {
    @Value("${spring.data.mongodb.database}")
    private String database;

    @NotNull
    @Override
    public MongoClient mongoClient() {
        return MongoClients.create();
    }

    @NotNull
    @Override
    protected String getDatabaseName() {
        return database;
    }
}
