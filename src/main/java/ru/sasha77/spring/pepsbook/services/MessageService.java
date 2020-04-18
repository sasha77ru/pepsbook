package ru.sasha77.spring.pepsbook.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.sasha77.spring.pepsbook.models.Message;
import ru.sasha77.spring.pepsbook.models.User;
import ru.sasha77.spring.pepsbook.repositories.MessageRepository;
import ru.sasha77.spring.pepsbook.repositories.UserRepository;

import java.util.Date;
import java.util.Objects;

import static ru.sasha77.spring.pepsbook.config.WebSocketConfig.MESSAGE_PREFIX;

@Service
public class MessageService {
    private UserRepository userRepository;
    private MessageRepository messageRepository;
    private InterlocService interlocService;
    private MongoOperations mongoOperations;
    private final SimpMessagingTemplate websocket;

    @Autowired
    public MessageService(UserRepository userRepository,
                          MessageRepository messageRepository,
                          InterlocService interlocService,
                          MongoOperations mongoOperations,
                          SimpMessagingTemplate websocket) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.interlocService = interlocService;
        this.mongoOperations = mongoOperations;
        this.websocket = websocket;
    }

    public Page<Message> loadMessages (Integer userId, Integer whomId, Integer page, Integer size, String subs) {
        Pageable pageable = page!=null ? PageRequest.of(page,size) : null;
        // TODO: 21.03.2020 findLike with subs
        return messageRepository.findLike(userId, whomId, pageable);
    }

    public String newMessage(String userName, Integer whomId, String text, Boolean unReady) {
        User user = Objects.requireNonNull(userRepository.findByUsername(userName),"No such userName");
        User whom = userRepository.findById(whomId).orElseThrow(() -> new NullPointerException("No such whomId"));

        Message message =  new Message(
                user.getName(),
                user.getId(),
                whom.getName(),
                whomId,
                new Date(),
                text,
                unReady
        );
        String id = messageRepository.save(message).get_id();

        if (unReady) interlocService.setHasPreMessages(whomId, user.getId(), whom.getUsername());
        else interlocService.incNumNewMessages(whomId, user.getId(), whom.getUsername());

        return id;
    }


    public void updateMessage(String _id, String userName, Integer whomId, String text, Boolean unReady, Date time) {
        User user = Objects.requireNonNull(userRepository.findByUsername(userName),"No such userName");
        User whom = userRepository.findById(whomId).orElseThrow(() -> new NullPointerException("No such whomId"));
        mongoOperations.updateFirst(new Query(Criteria.where("_id").is(_id)),
                new Update()
                        .set("text", text)
                        .set("unReady",unReady)
                        .set("time",time),
                Message.class);

        if (!unReady) interlocService.incNumNewMessages(whomId, user.getId(), whom.getUsername());

        try {
            JSONObject json = new JSONObject();
            json.put("_id",_id);
            json.put("text",text);
            this.websocket.convertAndSend(MESSAGE_PREFIX + "/updateMessage/"+whom.getId(), json.toString());
        } catch (JSONException ignored) {}
    }

    public void removeMessage(String messageId, Integer userId, Integer whomId) {
        User whom = userRepository.findById(whomId).orElseThrow(() -> new NullPointerException("No such whomId"));
        messageRepository.deleteBy_id(messageId);
        try {
            JSONObject json = new JSONObject();
            json.put("userId",userId);
            json.put("whoseId",whom);
            this.websocket.convertAndSend(MESSAGE_PREFIX + "/updateInterlocutors/"+whom.getId(), json.toString());
        } catch (JSONException ignored) {}
    }
}
