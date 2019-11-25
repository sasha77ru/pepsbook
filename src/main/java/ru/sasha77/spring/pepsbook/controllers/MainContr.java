package ru.sasha77.spring.pepsbook.controllers;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sasha77.spring.pepsbook.repositories.UserRepository;
import ru.sasha77.spring.pepsbook.security.JWTFilter;
import ru.sasha77.spring.pepsbook.security.TokenProvider;
import ru.sasha77.spring.pepsbook.webModels.UserLogin;
import ru.sasha77.spring.pepsbook.webModels.UserRegister;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@SuppressWarnings("JavaDoc")
@Controller
public class MainContr {
	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;
	private final TokenProvider tokenProvider;
	private final AuthenticationManagerBuilder authenticationManagerBuilder;

	public MainContr(UserRepository userRepository,
					 PasswordEncoder passwordEncoder,
					 TokenProvider tokenProvider,
					 AuthenticationManagerBuilder authenticationManagerBuilder
						  ) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenProvider = tokenProvider;
		this.authenticationManagerBuilder = authenticationManagerBuilder;
	}

	/**
	 * Opens main.html
	 * @return
	 */
	@RequestMapping(path = "/")
	public String index (HttpServletRequest request, @NotNull HttpServletResponse response) {
		response.addHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
		return "main.html";
	}


	@GetMapping(path = "/login")
	public String loginForm(UserLogin form) {
		return "login";
	}

	@PostMapping(path = "/login")
	public String submitLogin(@Valid UserLogin form,
									 Errors errors,
									 ModelMap model,
									 HttpServletRequest request,
									 HttpServletResponse response) {

		if (errors.hasErrors()) {
			return "login";
		}

		UsernamePasswordAuthenticationToken authenticationToken =
				new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword());
		Authentication authentication;
		try {
			authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
		} catch (AuthenticationException e) {
			errors.rejectValue("password","","Неправильный логин/пароль");
			return "login";
		}
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = tokenProvider.createToken(authentication, false/*rememberMe*/);
		response.addHeader(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
		model.addAttribute("jwt",jwt);
		return "welcome";
	}
	@GetMapping(path = "/register")
	public String registerForm(UserRegister form) {
		return "register";
	}

	@PostMapping(path = "/register")
	public String submitRegistration(@Valid UserRegister form,
									 Errors errors,
									 ModelMap model,
									 HttpServletRequest request,
									 HttpServletResponse response) {
		if (userRepository.findByUsername(form.getUsername()) != null) {
			errors.rejectValue("username","","Такой username уже существует");
		}
		if (userRepository.findByEmail(form.getEmail()) != null) {
			errors.rejectValue("email","","Такой email уже существует");
		}
		if (!form.getPassword().equals(form.getRepeatPassword())) {
			errors.rejectValue("repeatPassword","","Пароли не совпадают");
		}
		if (errors.hasErrors()) {
		    return "register";
        }
		userRepository.save(form.toUser(passwordEncoder));

		UsernamePasswordAuthenticationToken authenticationToken =
				new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword());
		Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = tokenProvider.createToken(authentication, false/*rememberMe*/);
		response.addHeader(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
		model.addAttribute("jwt",jwt);
		return "welcome";
	}
}
