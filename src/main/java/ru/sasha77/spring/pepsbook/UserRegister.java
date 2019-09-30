package ru.sasha77.spring.pepsbook;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
class UserRegister {

	@NotBlank(message = "username не указан")
	@Size(max=100, message = "Слишком длинный username")
	private String username;

	@NotBlank(message = "Нужен пароль")
	@Size(max=100, message = "Слишком длинный пароль")
	private String password;

	@NotBlank(message = "Нужен пароль")
	@Size(max=100, message = "Слишком длинный пароль")
	private String repeatPassword;

	@NotBlank(message = "Имя не указано")
	@Size(max=100, message = "Слишком длинное имя")
	private String name;

	@NotBlank(message = "email не указан")
	@Size(max=100, message = "Слишком длинный email")
	@Email(message = "Неправильный email")
	private String email;

	@NotBlank(message = "Страна не указана")
	@Size(max=100, message = "Слишком длинное название страны")
	private String country;

		User toUser(PasswordEncoder passwordEncoder) {
		return new User(name,email,country,username,passwordEncoder.encode(password));
	}
}

