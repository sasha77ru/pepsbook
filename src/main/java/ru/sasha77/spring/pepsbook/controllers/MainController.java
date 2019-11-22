package ru.sasha77.spring.pepsbook.controllers;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.servlet.ModelAndView;
import ru.sasha77.spring.pepsbook.models.User;
import ru.sasha77.spring.pepsbook.repositories.MindRepository;
import ru.sasha77.spring.pepsbook.repositories.UserRepository;
import ru.sasha77.spring.pepsbook.security.JWTFilter;
import ru.sasha77.spring.pepsbook.security.TokenProvider;
import ru.sasha77.spring.pepsbook.webModels.UserLogin;
import ru.sasha77.spring.pepsbook.webModels.UserRegister;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Comparator;
import java.util.stream.Collectors;

@SuppressWarnings("JavaDoc")
@Controller
public class MainController {
	private final UserRepository userRepository;
	private final MindRepository mindRepository;

	private final PasswordEncoder passwordEncoder;
	private final TokenProvider tokenProvider;
	private final AuthenticationManagerBuilder authenticationManagerBuilder;

	public MainController(UserRepository userRepository,
						  MindRepository mindRepository,
						  PasswordEncoder passwordEncoder,
						  TokenProvider tokenProvider,
						  AuthenticationManagerBuilder authenticationManagerBuilder
						  ) {
		this.userRepository = userRepository;
		this.mindRepository = mindRepository;
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

		Authentication authentication = null;
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

////        securityService.autologin(form.getUsername(), form.getPassword());
//		try {
//			request.login(form.getUsername(), form.getPassword());
//		} catch (ServletException e) {
//			return "register";
//		}
		model.addAttribute("jwt",jwt);
		return "welcome";
	}

	/**
	 * Write html table with users, matching to filter
	 * @param subs string for filter
	 * @return
	 */
	@GetMapping(path="/users")
	public ModelAndView users (@NotNull Principal principal, String subs) {
		User user = userRepository.findByUsername(principal.getName());
		ModelAndView mv = new ModelAndView();
		mv.setViewName("users");
		mv.addObject("currUser",user);
		mv.addObject("lizt", userRepository.findLike(subs!=null?subs:"",user.getId()));
		return mv;
	}

	/**
	 * Write html table with friends, matching to filter
	 * @return
	 */
	@GetMapping(path="/friends")
	public ModelAndView friends (@NotNull Principal principal) {
		User user = userRepository.findByUsername(principal.getName());
		ModelAndView mv = new ModelAndView();
		mv.setViewName("users");
		mv.addObject("currUser",user);
		mv.addObject("lizt", user.getFriends().stream().sorted(Comparator.comparing(User::getName)).collect(Collectors.toList()));
		return mv;
	}

	/**
	 * Write html table with mates, matching to filter
	 * @return
	 */
	@GetMapping(path="/mates")
	public ModelAndView mates (@NotNull Principal principal) {
		User user = userRepository.findByUsername(principal.getName());
		ModelAndView mv = new ModelAndView();
		mv.setViewName("users");
		mv.addObject("currUser",user);
		mv.addObject("lizt", user.getMates().stream().sorted(Comparator.comparing(User::getName)).collect(Collectors.toList()));
		return mv;
	}

	/**
	 * Write html table with minds, matching to filter
	 * @param subs string for filter
	 * @return ModelAndView
	 */
	@GetMapping(path="/minds")
	public ModelAndView minds (@NotNull Principal principal, String subs) {
		User user = userRepository.findByUsername(principal.getName());
		ModelAndView mv = new ModelAndView();
		mv.setViewName("minds");
		mv.addObject("currUser", user);
		mv.addObject("lizt", mindRepository.findLike(subs==null?"":subs));
		return mv;
	}
}
