package ru.sasha77.spring.pepsbook.controllers;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;
import ru.sasha77.spring.pepsbook.models.Answer;
import ru.sasha77.spring.pepsbook.models.Message;
import ru.sasha77.spring.pepsbook.models.Mind;
import ru.sasha77.spring.pepsbook.models.User;
import ru.sasha77.spring.pepsbook.repositories.UserRepository;
import ru.sasha77.spring.pepsbook.services.InterlocService;
import ru.sasha77.spring.pepsbook.services.MessageService;
import ru.sasha77.spring.pepsbook.services.MindService;
import ru.sasha77.spring.pepsbook.services.UserService;
import ru.sasha77.spring.pepsbook.webModels.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("JavaDoc")
@RestController
@RequestMapping(path = "/rest")
@EnableTransactionManagement
public class RestContr {
    private UserRepository userRepository;
    private UserService userService;
    private MindService mindService;
    private MessageService messageService;
    private InterlocService interlocService;

    RestContr(UserRepository userRepository,
              UserService userService,
              MindService mindService,
              MessageService messageService,
              InterlocService interlocService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.mindService = mindService;
        this.messageService = messageService;
        this.interlocService = interlocService;
    }

    /**
     * Returns UserSimple for current user
     * @return UserSimple for current user
     * @throws IOException
     */
    @GetMapping(path = "/getUser", produces = "application/json")
    public UserSimple getUser(@NotNull Principal principal, HttpServletResponse response) throws IOException {
        User user = userRepository.findByUsername(principal.getName());
        if (user == null) {response.sendError(HttpServletResponse.SC_FORBIDDEN);return null;}
        return new UserSimple(user);
    }

    /**
     * @return Users except current user
     */
    @GetMapping(path = "/users", produces = "application/json")
    public List<UsersResponse> getUsers(@NotNull Principal principal, String subs) {
        User user = userRepository.findByUsername(principal.getName());
        return userRepository.findLike(subs!=null?subs:"",user.getId()).stream()
                .map(it -> new UsersResponse(it,user))
                .collect(Collectors.toList());
    }

    /**
     * @return Current user's friends
     */
    @GetMapping(path = "/friends", produces = "application/json")
    public List<UsersResponse> getFriends(@NotNull Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        return user.getFriends().stream()
                .sorted(Comparator.comparing(User::getName))
                .map(it -> new UsersResponse(it,user))
                .collect(Collectors.toList());
    }

    /**
     * @return Current user's mates
     */
    @GetMapping(path = "/mates", produces = "application/json")
    public List<UsersResponse> getMates(@NotNull Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        return user.getMates().stream()
                .sorted(Comparator.comparing(User::getName))
                .map(it -> new UsersResponse(it,user))
                .collect(Collectors.toList());
    }

    /**
     * @return Page of minds
     */
    @GetMapping(path = "/minds", produces = "application/json")
    public Page<MindsResponse> getMinds(@NotNull Authentication authentication, String subs, Integer page, Integer size) {
        User user = userRepository.findByUsername(authentication.getName());
        Page<Mind> pagePage = mindService.loadMinds(subs,user,page,size);
        //if such page doesn't exist anymore, return last page
        if (page != null && page != 0 && pagePage.getTotalPages() <= page && pagePage.getTotalPages() != 0)
            pagePage =  mindService.loadMinds(subs,user,pagePage.getTotalPages()-1,size);
        return pagePage.map(it -> new MindsResponse(it,authentication.getName()));
    }

    /**
     * Adds friend to friends for current user. Returns mutuality
     * @param friend_id
     * @param response
     * @return "mutual" if they are now mutual friends or "halfFriend" otherwise
     * @throws IOException
     */
    @SuppressWarnings("Duplicates")
    @PatchMapping(path = "/toFriends")
    public String toFriends(@NotNull Principal principal, @RequestParam int friend_id,
                            HttpServletResponse response) throws IOException {
        User user = userRepository.findByUsername(principal.getName());
        User friend = userRepository.findById(friend_id).orElse(null);
        if (friend == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return null;}
        userService.toFriends(user,friend);
        return (user.getMates().contains(friend))?"mate":"notMate";
    }

    /**
     * Remove friend from friends for current user. Returns mateness
     * @param friend_id
     * @param response
     * @return "mutual" if they were or "nobody" otherwise
     * @throws IOException
     */
    @SuppressWarnings("Duplicates")
    @PatchMapping(path = "/fromFriends")
    public String fromFriends(Principal principal, @RequestParam int friend_id,
                              HttpServletResponse response) throws IOException {
        User user = userRepository.findByUsername(principal.getName());
        User friend = userRepository.findById(friend_id).orElse(null);
        if (friend == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return null;}
        userService.fromFriends(user,friend);
        return (user.getMates().contains(friend))?"mate":"notMate";
    }

    /**
     * Adds mind or update existing (if id isn't null)
     * @param text New text of mind
     * @param id If isn't null - id for mind to update
     * @param response
     * @throws IOException
     */
    @PostMapping(path = "/saveMind")
    @SuppressWarnings("Duplicates")
    public void saveMind(Principal principal,
                           @RequestParam String text,
                           Integer id,
                           HttpServletResponse response) throws IOException {
        Mind mind = (id == null || id == 0) ?
                new Mind(text,userRepository.findByUsername(principal.getName())) : mindService.getMind(id);
        if (mind == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return;}
        mind.setText(text);
        if (!mind.getUser().getUsername().equals(principal.getName())) {response.sendError(HttpServletResponse.SC_FORBIDDEN);return;}
        mindService.saveMind(mind);
    }

    /**
     * Removes mind from DB
     * @param id
     * @param response
     * @throws IOException
     */
    @DeleteMapping(path = "/removeMind")
    @SuppressWarnings("Duplicates")
    public void removeMind(Authentication authentication,
                           @RequestParam Integer id,
                           HttpServletResponse response) throws IOException {
        Mind mind = mindService.getMind(id);
        if (mind == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return;}
        if (!mind.getUser().getUsername().equals(authentication.getName()))
            {response.sendError(HttpServletResponse.SC_FORBIDDEN);return;}
        mindService.deleteMind(mind);
    }

    /**
     * Adds mind or update existing (if id isn't null)
     * @param text New text of mind
     * @param id If isn't null - id for mind to update
     * @param response
     * @throws IOException
     */
    @PostMapping(path = "/saveAnswer")
    @SuppressWarnings("Duplicates")
    public void saveAnswer(Principal principal,
                           @RequestParam String text,
                           Integer id,
                           @RequestParam Integer parentMind,
                           HttpServletResponse response) throws IOException {
        Mind mind = mindService.getMind(parentMind);
        if (mind == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return;}
        Answer answer = (id == null || id == 0) ?
                new Answer(text,mind,userRepository.findByUsername(principal.getName())) : mindService.getAnswer(id);
        if (answer == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return;}
        answer.setText(text);
        if (!answer.getUser().getUsername().equals(principal.getName())) {response.sendError(HttpServletResponse.SC_FORBIDDEN);return;}
        mindService.saveAnswer(answer);
    }

    /**
     * Removes mind from DB
     * @param id
     * @param response
     * @throws IOException
     */
    @DeleteMapping(path = "/removeAnswer")
    @SuppressWarnings("Duplicates")
    public void removeAnswer(Authentication authentication,
                             @RequestParam Integer id,
                             HttpServletResponse response) throws IOException {
        Answer answer = mindService.getAnswer(id);
        if (answer == null) {response.sendError(HttpServletResponse.SC_NOT_FOUND);return;}
        if (!answer.getUser().getUsername().equals(authentication.getName()))
            {response.sendError(HttpServletResponse.SC_FORBIDDEN);return;}
        mindService.deleteAnswer(answer);
    }


    /**
     * @return Page of messages. And clears interlocutor's counters
     */
    @GetMapping(path = "/messages", produces = "application/json")
    public Page<MessagesResponse> getMessages(@NotNull Authentication authentication,
                                           Integer whomId,
                                           Integer page,
                                           Integer size,
                                           String subs) {
        User user = userRepository.findByUsername(authentication.getName());
        Integer userId = user.getId();
        //If user reads messages on 0 page. it means he see new messages, we should clear interlocutor's counters
        if (page == 0) interlocService.clearInterlocutorState(userId, whomId, authentication.getName());

        Page<Message> pagePage = messageService.loadMessages(userId,whomId,page,size,subs);
        //if such page doesn't exist anymore, return last page
        if (page != null && page != 0 && pagePage.getTotalPages() <= page && pagePage.getTotalPages() != 0)
            pagePage =  messageService.loadMessages(userId,whomId,pagePage.getTotalPages()-1,size,subs);
        return pagePage.map(MessagesResponse::new);
    }

    @PostMapping(path = "/newMessage")
    public String newMessage(@NotNull Authentication authentication,
                           @RequestParam Integer whomId,
                           @RequestParam String text,
                           Boolean unReady) {
        return messageService.newMessage(authentication.getName(),whomId,text,unReady);
    }

    @PatchMapping(path = "/updateMessage")
    public void updateMessage(@NotNull Authentication authentication,
                           @RequestParam String _id,
                           @RequestParam String text,
                           @RequestParam Integer whomId,
                           Boolean unReady) {
        messageService.updateMessage(_id, authentication.getName(), whomId, text, unReady, new Date());
    }

    @DeleteMapping(path = "/removeMessage")
    public void removeMessage(@NotNull Authentication authentication,
                              @RequestParam String _id,
                              @RequestParam Integer whomId
                              ) {
        User user = userRepository.findByUsername(authentication.getName());
        messageService.removeMessage(user.getId(), authentication.getName(), whomId);
    }

    /**
     * @return List of interlocutors
     */
    @GetMapping(path = "/interlocutors", produces = "application/json")
    public List getInterlocutors(@NotNull Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName());
        return interlocService.loadInterlocutors(user.getId())
                .stream().map(InterlocResponse::new).collect(Collectors.toList());
    }

    @PostMapping(path = "/startMessaging")
    public void startMessaging(@NotNull Authentication authentication,
                           @RequestParam Integer whomId) {
        User user = Objects.requireNonNull(userRepository.findByUsername(authentication.getName()),"No such user");
        User interlocutor = userRepository.findById(whomId).orElseThrow(() -> new NullPointerException("No such whomId"));
        interlocService.newInterlocutors(user,interlocutor);
    }
}
