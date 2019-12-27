package ru.sasha77.spring.pepsbook;

import org.springframework.beans.factory.annotation.Autowired;
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
// See only friends minds
// Friends-only tick for minds
// Labels in registration form