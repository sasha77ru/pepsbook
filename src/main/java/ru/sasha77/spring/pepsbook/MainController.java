package ru.sasha77.spring.pepsbook;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Comparator;
import java.util.stream.Collectors;

@SuppressWarnings("JavaDoc")
@Controller
public class MainController {
	private final UserRepository userRepository;
	private final MindRepository mindRepository;

	final
	PasswordEncoder passwordEncoder;

	public MainController(UserRepository userRepository, MindRepository mindRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.mindRepository = mindRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Opens main.html
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(path = "/")
	public String index (HttpServletRequest request, @NotNull HttpServletResponse response) {
		response.addHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
		return "main.html";
	}

	@GetMapping(path = "/register")
	public String registerForm() {
		return "register.html";
	}

	@PostMapping(path = "/register")
	public String processRegistration(UserRegister form) {
		userRepository.save(form.toUser(passwordEncoder));
		return "redirect:/";
	}

	/**
	 * Write html table with users, matching to filter
	 * @param subs
	 * @return
	 * @throws IOException
	 */
	@GetMapping(path="/users")
	public ModelAndView users (@NotNull Principal principal, String subs) {
		User user = userRepository.findByUsername(principal.getName());
		ModelAndView mv = new ModelAndView();
		mv.setViewName("WEB-INF/users.jsp");
		mv.addObject("currUser",user);
		mv.addObject("lizt", userRepository.findLike(subs!=null?subs:"",user.getId()));
		return mv;
	}

	/**
	 * Write html table with friends, matching to filter
	 * @param subs
	 * @return
	 */
	@GetMapping(path="/friends")
	public ModelAndView friends (@NotNull Principal principal, String subs) {
		User user = userRepository.findByUsername(principal.getName());
		ModelAndView mv = new ModelAndView();
		mv.setViewName("/WEB-INF/users.jsp");
		mv.addObject("currUser",user);
		mv.addObject("lizt", user.getFriends().stream().sorted(Comparator.comparing(User::getName)).collect(Collectors.toList()));
		return mv;
	}

	/**
	 * Write html table with mates, matching to filter
	 * @param subs
	 * @return
	 */
	@GetMapping(path="/mates")
	public ModelAndView mates (@NotNull Principal principal, String subs) {
		User user = userRepository.findByUsername(principal.getName());
		ModelAndView mv = new ModelAndView();
		mv.setViewName("/WEB-INF/users.jsp");
		mv.addObject("currUser",user);
		mv.addObject("lizt", user.getMates().stream().sorted(Comparator.comparing(User::getName)).collect(Collectors.toList()));
		return mv;
	}

	/**
	 * Write html table with minds, matching to filter
	 * @param subs
	 * @return ModelAndView
	 */
	@GetMapping(path="/minds")
	public ModelAndView minds (@NotNull Principal principal, String subs) {
		User user = userRepository.findByUsername(principal.getName());
		ModelAndView mv = new ModelAndView();
		mv.setViewName("/WEB-INF/minds.jsp");
		mv.addObject("currUser", user);
		mv.addObject("lizt", mindRepository.findLike(subs==null?"":subs));
		return mv;
	}
}
