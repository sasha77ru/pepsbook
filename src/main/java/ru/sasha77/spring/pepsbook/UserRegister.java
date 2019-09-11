package ru.sasha77.spring.pepsbook;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
class UserRegister {

    private String name;
	private String email;
	private String country;
	private String username;
	private String password;

	User toUser(PasswordEncoder passwordEncoder) {
		return new User(name,email,country,username,passwordEncoder.encode(password));
	}
}

