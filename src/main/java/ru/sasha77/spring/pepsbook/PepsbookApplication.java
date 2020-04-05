package ru.sasha77.spring.pepsbook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import ru.sasha77.spring.pepsbook.models.Message;
import ru.sasha77.spring.pepsbook.repositories.MessageRepository;
import ru.sasha77.spring.pepsbook.services.MindService;

@SpringBootApplication
@EnableCaching
public class PepsbookApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(PepsbookApplication.class, args);
		context.getBean(MindService.class).clearCache();
	}
}

/*
 * ### ### ##  ###
 *  #  # # # # # #
 *  #  ### ##  ###
 */

// Pagination in users
// Friends-only tick for minds
// Labels and pass fields in registration form