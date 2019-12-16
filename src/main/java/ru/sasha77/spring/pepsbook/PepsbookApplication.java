package ru.sasha77.spring.pepsbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PepsbookApplication {
	public static void main(String[] args) {
		SpringApplication.run(PepsbookApplication.class, args);
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