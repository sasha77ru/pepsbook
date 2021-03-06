package ru.sasha77.spring.pepsbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
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