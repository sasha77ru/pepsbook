package ru.sasha77.spring.pepsbook;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static ru.sasha77.spring.pepsbook.ToolsKt.getNewCookie;

@SuppressWarnings("JavaDoc")
@Controller
@RequestMapping(path = "/rest")
@EnableTransactionManagement
public class RestController {
    private UserRepository userRepository;
    private MindRepository mindRepository;

    RestController(UserRepository userRepository, MindRepository mindRepository) {
        this.userRepository = userRepository;
        this.mindRepository = mindRepository;
    }

    /**
     * Returns UserSimple JSON with all users
     * @return UserSimple JSON with all users
     * @throws IOException
     */
    @GetMapping(path = "/allUsersSimple", produces = "application/json")
    @ResponseBody
    @Transactional
    public Iterable<UserSimple> users () {
        return StreamSupport.stream(userRepository.findAll().spliterator(),false)
                .map (it -> new UserSimple(it.getId(), it.getName(), it.getEmail())).collect(Collectors.toList());
    }



    /**
     * Check user by their login (email) and set key cookie
     * @param login the login (actually email)
     * @param response
     * @throws IOException
     */
    @GetMapping(path = "/checkUser")
    @ResponseBody
    @Transactional
    public void checkUser(@RequestParam String login,
                          HttpServletResponse response) throws IOException, InterruptedException {
        User user = userRepository.findUserByEmail(login);
        if (user == null) {
            Cookie cookie = new Cookie("keyCookie",null);
            cookie.setMaxAge(-1);
            cookie.setPath("/");
            response.addCookie(cookie);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            Thread.sleep(1000);
            return;
        }
        Cookie cookie = new Cookie("keyCookie",user.getKeyCookie());
        cookie.setMaxAge(86400*365*10);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * Returns UserSimple for current user
     * @param keyCookie
     * @param response
     * @return UserSimple for current user
     * @throws IOException
     */
    @GetMapping(path = "/userByCookie", produces = "application/json")
    @ResponseBody
    @Transactional
    public UserSimple userByCookie(@CookieValue("keyCookie") String keyCookie,
                                   HttpServletResponse response) throws IOException, InterruptedException {
        User user = userRepository.findUserByKeyCookie(keyCookie);
        if (user == null) {response.sendError(HttpServletResponse.SC_UNAUTHORIZED);Thread.sleep(1000);return null;}
        return new UserSimple(user.getId(), user.getName(), user.getEmail());
    }

    /**
     * Erase keyCookie (just logOff). If all==true - change keyCookie in DB (logOff from all devices)
     * @param all logOff from all devices
     * @param keyCookie
     * @param response
     * @throws IOException
     */
    @PatchMapping(path = "/logOff")
    @ResponseBody
    @Transactional
    public void logOff(Boolean all,
                       @CookieValue("keyCookie") String keyCookie,
                       HttpServletResponse response) throws IOException, InterruptedException {
        Cookie cookie = new Cookie("keyCookie",null);
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        response.addCookie(cookie);
        if (all !=null && all) {
            User currUser = userRepository.findUserByKeyCookie(keyCookie);
            if (currUser == null) {response.sendError(HttpServletResponse.SC_UNAUTHORIZED);Thread.sleep(1000);return;}
            currUser.setKeyCookie(getNewCookie());
            userRepository.save(currUser);
        }
    }

    /**
     * Adds friend to friends for current user. Returns mutuality
     * @param keyCookie
     * @param friend_id
     * @param response
     * @return "mutual" if they are now mutual friends or "halfFriend" otherwise
     * @throws IOException
     */
    @PatchMapping(path = "/toFriends")
    @ResponseBody
    @Transactional
    @SuppressWarnings("Duplicates")
    public String toFriends(@CookieValue("keyCookie") String keyCookie,
                            @RequestParam int friend_id,
                            HttpServletResponse response) throws IOException, InterruptedException {
        User currUser = userRepository.findUserByKeyCookie(keyCookie);
        if (currUser == null) {response.sendError(HttpServletResponse.SC_UNAUTHORIZED);Thread.sleep(1000);return null;}
        User friend = userRepository.findById(friend_id).orElse(null);
        if (friend == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return null;}
        currUser.getFriends().add(friend);
        userRepository.save(currUser);
        return (currUser.getMates().contains(friend))?"mutual":"halfFriend";
    }

    /**
     * Remove friend from friends for current user. Returns mateness
     * @param keyCookie
     * @param friend_id
     * @param response
     * @return "mutual" if they were or "nobody" otherwise
     * @throws IOException
     */
    @PatchMapping(path = "/fromFriends")
    @ResponseBody
    @Transactional
    @SuppressWarnings("Duplicates")
    public String fromFriends(@CookieValue("keyCookie") String keyCookie,
                              @RequestParam int friend_id,
                              HttpServletResponse response) throws IOException, InterruptedException {
        User currUser = userRepository.findUserByKeyCookie(keyCookie);
        if (currUser == null) {response.sendError(HttpServletResponse.SC_UNAUTHORIZED);Thread.sleep(1000);return null;}
        User friend = userRepository.findById(friend_id).orElse(null);
        if (friend == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return null;}
        currUser.getFriends().remove(friend);
        userRepository.save(currUser);
        return (currUser.getMates().contains(friend))?"mutual":"nobody";
    }

    /**
     * Adds mind or update existing (if id isn't null)
     * @param keyCookie
     * @param text New text of mind
     * @param id If isn't null - id for mind to update
     * @param response
     * @throws IOException
     */
    @PostMapping(path = "/saveMind")
    @ResponseBody
    @Transactional
    @SuppressWarnings("Duplicates")
    public void saveMind(@CookieValue("keyCookie") String keyCookie,
                           String text,
                           Integer id,
                           HttpServletResponse response) throws IOException, InterruptedException {
        User currUser = userRepository.findUserByKeyCookie(keyCookie);
        if (currUser == null) {response.sendError(HttpServletResponse.SC_UNAUTHORIZED);Thread.sleep(1000);return;}
        Mind mind = (id == null || id == 0) ? new Mind(text,currUser) : mindRepository.findById(id).orElse(null);
        if (mind == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return;}
        mind.setText(text);
        if (!mind.getUser().getId().equals(currUser.getId())) {response.sendError(HttpServletResponse.SC_FORBIDDEN);return;}
        mindRepository.save(mind);
    }

    /**
     * Removes mind from DB
     * @param keyCookie
     * @param id
     * @param response
     * @throws IOException
     */
    @DeleteMapping(path = "/removeMind")
    @ResponseBody
    @Transactional
    @SuppressWarnings("Duplicates")
    public void removeMind(@CookieValue("keyCookie") String keyCookie,
                             Integer id,
                             HttpServletResponse response) throws IOException, InterruptedException {
        User currUser = userRepository.findUserByKeyCookie(keyCookie);
        if (currUser == null) {response.sendError(HttpServletResponse.SC_UNAUTHORIZED);Thread.sleep(1000);return;}
        Mind mind = mindRepository.findById(id).orElse(null);
        if (mind == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return;}
        if (!mind.getUser().getId().equals(currUser.getId())) {response.sendError(HttpServletResponse.SC_FORBIDDEN);return;}
        mindRepository.delete(mind);
    }

    /**
     * Adds a new user to DB. Set a keyCookie for them. And redirects to the main page.
     * @param name
     * @param email
     * @param country
     * @param response
     * @return Redirects to main page
     */
    @PostMapping(path = "/addUser")
    @Transactional
    public String addUser(String name, String email, String country,
                          HttpServletResponse response) {
        User currUser = new User(name, email, country);
        userRepository.save(currUser);
        Cookie cookie = new Cookie("keyCookie",currUser.getKeyCookie());
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/";
    }
}

