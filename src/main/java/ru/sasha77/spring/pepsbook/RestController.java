package ru.sasha77.spring.pepsbook;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.security.Principal;

@SuppressWarnings("JavaDoc")
@Controller
@RequestMapping(path = "/rest")
@EnableTransactionManagement
public class RestController {
    private UserRepository userRepository;
    private MindRepository mindRepository;
    private AnswerRepository answerRepository;

    RestController(UserRepository userRepository, MindRepository mindRepository, AnswerRepository answerRepository) {
        this.userRepository = userRepository;
        this.mindRepository = mindRepository;
        this.answerRepository = answerRepository;
    }

    /**
     * Returns UserSimple for current user
     * @return UserSimple for current user
     * @throws IOException
     */
    @GetMapping(path = "/getUser", produces = "application/json")
    @ResponseBody
    @Transactional
    public UserSimple getUser(@NotNull Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        return user.getUserSimple();
    }

    /**
     * Adds friend to friends for current user. Returns mutuality
     * @param friend_id
     * @param response
     * @return "mutual" if they are now mutual friends or "halfFriend" otherwise
     * @throws IOException
     */
    @PatchMapping(path = "/toFriends")
    @ResponseBody
    @Transactional
    public String toFriends(@NotNull Principal principal, @RequestParam int friend_id,
                            HttpServletResponse response) throws IOException {
        User user = userRepository.findByUsername(principal.getName());
        User friend = userRepository.findById(friend_id).orElse(null);
        if (friend == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return null;}
        user.getFriends().add(friend);
        userRepository.save(user);
        return (user.getMates().contains(friend))?"mutual":"halfFriend";
    }

    /**
     * Remove friend from friends for current user. Returns mateness
     * @param friend_id
     * @param response
     * @return "mutual" if they were or "nobody" otherwise
     * @throws IOException
     */
    @PatchMapping(path = "/fromFriends")
    @ResponseBody
    @Transactional
    public String fromFriends(Principal principal, @RequestParam int friend_id,
                              HttpServletResponse response) throws IOException {
        User user = userRepository.findByUsername(principal.getName());
        User friend = userRepository.findById(friend_id).orElse(null);
        if (friend == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return null;}
        user.getFriends().remove(friend);
        userRepository.save(user);
        return (user.getMates().contains(friend))?"mutual":"nobody";
    }

    /**
     * Adds mind or update existing (if id isn't null)
     * @param text New text of mind
     * @param id If isn't null - id for mind to update
     * @param response
     * @throws IOException
     */
    @PostMapping(path = "/saveMind")
    @ResponseBody
    @Transactional
    @SuppressWarnings("Duplicates")
    public void saveMind(Principal principal,
                           String text,
                           Integer id,
                           HttpServletResponse response) throws IOException {
        User user = userRepository.findByUsername(principal.getName());
        Mind mind = (id == null || id == 0) ? new Mind(text,user) : mindRepository.findById(id).orElse(null);
        if (mind == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return;}
        mind.setText(text);
        if (!mind.getUser().getId().equals(user.getId())) {response.sendError(HttpServletResponse.SC_FORBIDDEN);return;}
        mindRepository.save(mind);
    }

    /**
     * Removes mind from DB
     * @param id
     * @param response
     * @throws IOException
     */
    @DeleteMapping(path = "/removeMind")
    @ResponseBody
    @Transactional
    @SuppressWarnings("Duplicates")
    public void removeMind(Principal principal,
                           Integer id,
                           HttpServletResponse response) throws IOException {
        User user = userRepository.findByUsername(principal.getName());
        Mind mind = mindRepository.findById(id).orElse(null);
        if (mind == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return;}
        if (!mind.getUser().getId().equals(user.getId())) {response.sendError(HttpServletResponse.SC_FORBIDDEN);return;}
        mindRepository.delete(mind);
    }

    /**
     * Adds mind or update existing (if id isn't null)
     * @param text New text of mind
     * @param id If isn't null - id for mind to update
     * @param response
     * @throws IOException
     */
    @PostMapping(path = "/saveAnswer")
    @ResponseBody
    @Transactional
    @SuppressWarnings("Duplicates")
    public void saveAnswer(Principal principal,
                           String text,
                           Integer id,
                           @RequestParam Integer parentMind,
                           HttpServletResponse response) throws IOException {
        User user = userRepository.findByUsername(principal.getName());
        Mind mind = mindRepository.findById(parentMind).orElse(null);
        if (mind == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return;}
        Answer answer = (id == null || id == 0) ? new Answer(text,mind,user) : answerRepository.findById(id).orElse(null);
        if (answer == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return;}
        answer.setText(text);
        if (!answer.getUser().getId().equals(user.getId())) {response.sendError(HttpServletResponse.SC_FORBIDDEN);return;}
        answerRepository.save(answer);
    }

    /**
     * Removes mind from DB
     * @param id
     * @param response
     * @throws IOException
     */
    @DeleteMapping(path = "/removeAnswer")
    @ResponseBody
    @Transactional
    @SuppressWarnings("Duplicates")
    public void removeAnswer(Principal principal,
                             Integer id,
                             HttpServletResponse response) throws IOException {
        User user = userRepository.findByUsername(principal.getName());
        Answer answer = answerRepository.findById(id).orElse(null);
        if (answer == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return;}
        if (!answer.getUser().getId().equals(user.getId())) {response.sendError(HttpServletResponse.SC_FORBIDDEN);return;}
        answerRepository.delete(answer);
    }
}
