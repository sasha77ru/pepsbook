package ru.sasha77.spring.pepsbook.webModels;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UserLogin {

	@NotBlank(message = "username не указан")
	@Pattern(regexp = "^[A-Za-z][-_A-za-z0-9]*$", message = "Неправильные символы в username")
	@Size(max=100, message = "Слишком длинный username")
	private String username;

	@NotBlank(message = "Нужен пароль")
	@Pattern(regexp = "^[A-Za-z][-_A-za-z0-9]*$", message = "Неправильные символы в пароле")
	@Size(max=100, message = "Слишком длинный пароль")
	private String password;

}

