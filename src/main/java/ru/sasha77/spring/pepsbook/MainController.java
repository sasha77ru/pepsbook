package ru.sasha77.spring.pepsbook;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Comparator;
import java.util.stream.Collectors;

@SuppressWarnings("JavaDoc")
@Controller
public class MainController {
	private final UserRepository userRepository;
	private final MindRepository mindRepository;

	public MainController(UserRepository userRepository, MindRepository mindRepository) {
		this.userRepository = userRepository;
		this.mindRepository = mindRepository;
	}

	/**
	 * If keyCookie exists and matches user entry - open main.html otherwise login.html
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(path = "/")
	public String index (HttpServletRequest request, HttpServletResponse response) {
		response.addHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
		Cookie cookie = WebUtils.getCookie(request, "keyCookie");
		if (cookie == null
				||cookie.getValue().isEmpty()
				||!userRepository.existsUserByKeyCookie(cookie.getValue()))
			return "login.html"; else return "main.html";
	}

	/**
	 * Write html table with users, matching to filter
	 * @param keyCookie
	 * @param subs
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@GetMapping(path="/users")
	public ModelAndView users (@CookieValue("keyCookie") String keyCookie,
							   String subs,
							   HttpServletResponse response) throws IOException, InterruptedException {
		User user = userRepository.findUserByKeyCookie(keyCookie);
		if (user == null) {response.sendError(HttpServletResponse.SC_UNAUTHORIZED);Thread.sleep(1000);return null;}

		ModelAndView mv = new ModelAndView();
		mv.setViewName("WEB-INF/users.jsp");
		mv.addObject("currUser",user);
		mv.addObject("lizt", userRepository.findLike(subs!=null?subs:"",user.getId()));
//		mv.addObject("lizt", userRepository.findUsersByNameContainsAndIdIsNotOrCountryContainsAndIdIsNot(subs,currUser.getId(),subs,currUser.getId()));
		return mv;
	}

	/**
	 * Write html table with friends, matching to filter
	 * @param subs
	 * @param keyCookie
	 * @return
	 */
	@SuppressWarnings("Duplicates")
	@GetMapping(path="/friends")
	public ModelAndView friends (@CookieValue("keyCookie") String keyCookie,
								 String subs,
								 HttpServletResponse response) throws InterruptedException, IOException {
		User currUser = userRepository.findUserByKeyCookie(keyCookie);
		if (currUser == null) {response.sendError(HttpServletResponse.SC_UNAUTHORIZED);Thread.sleep(1000);return null;}

		ModelAndView mv = new ModelAndView();

		mv.setViewName("/WEB-INF/users.jsp");
		mv.addObject("currUser",currUser);
		mv.addObject("lizt", currUser.getFriends().stream().sorted(Comparator.comparing(User::getName)).collect(Collectors.toList()));

		return mv;
	}

	/**
	 * Write html table with mates, matching to filter
	 * @param subs
	 * @param keyCookie
	 * @return
	 */
	@SuppressWarnings("Duplicates")
	@GetMapping(path="/mates")
	public ModelAndView mates (@CookieValue("keyCookie") String keyCookie,
								 String subs,
							     HttpServletResponse response) throws InterruptedException, IOException {

		User currUser = userRepository.findUserByKeyCookie(keyCookie);
		if (currUser == null) {response.sendError(HttpServletResponse.SC_UNAUTHORIZED);Thread.sleep(1000);return null;}

		ModelAndView mv = new ModelAndView();

		mv.setViewName("/WEB-INF/users.jsp");
		mv.addObject("currUser",currUser);
		mv.addObject("lizt", currUser.getMates().stream().sorted(Comparator.comparing(User::getName)).collect(Collectors.toList()));

		return mv;
	}

	/**
	 * Write html table with minds, matching to filter
	 * @param subs
	 * @param keyCookie
	 * @return
	 */
	@GetMapping(path="/minds")
	public ModelAndView minds (@CookieValue("keyCookie") String keyCookie,
								 String subs,
								 HttpServletResponse response) throws InterruptedException, IOException {

		User currUser = userRepository.findUserByKeyCookie(keyCookie);
		if (currUser == null) {response.sendError(HttpServletResponse.SC_UNAUTHORIZED);Thread.sleep(1000);return null;}

		ModelAndView mv = new ModelAndView();

		mv.setViewName("/WEB-INF/minds.jsp");
		mv.addObject("currUser", currUser);
		mv.addObject("lizt", mindRepository.findLike(subs==null?"":subs));

		return mv;
	}
}
