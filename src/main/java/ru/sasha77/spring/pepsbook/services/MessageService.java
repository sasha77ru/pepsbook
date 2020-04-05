package ru.sasha77.spring.pepsbook.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import ru.sasha77.spring.pepsbook.models.Message;
import ru.sasha77.spring.pepsbook.models.User;
import ru.sasha77.spring.pepsbook.repositories.MessageRepository;
import ru.sasha77.spring.pepsbook.repositories.UserRepository;

import java.util.Date;
import java.util.Objects;

@Service
public class MessageService {
    private UserRepository userRepository;
    private MessageRepository messageRepository;
    private InterlocService interlocService;
    private MongoOperations mongoOperations;
    @Autowired
    public MessageService(UserRepository userRepository,
                          MessageRepository messageRepository,
                          InterlocService interlocService,
                          MongoOperations mongoOperations) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.interlocService = interlocService;
        this.mongoOperations = mongoOperations;
    }

    public Page<Message> loadMessages (Integer userId, Integer whomId, Integer page, Integer size, String subs) {
        Pageable pageable = page!=null ? PageRequest.of(page,size) : null;
        // TODO: 21.03.2020 findLike with subs
        return messageRepository.findLike(userId, whomId, pageable);
    }

    public String newMessage(String userName, Integer whomId, String text, Boolean unReady) {
        User user = Objects.requireNonNull(userRepository.findByUsername(userName),"No such userName");

        interlocService.incNumNewMessages(user.getId(),whomId);

        Message message =  new Message(
                user.getName(),
                user.getId(),
                userRepository.findById(whomId).orElseThrow(() -> new NullPointerException("No such whomId")).getName(),
                whomId,
                new Date(),
                text,
                unReady
        );
        return messageRepository.save(message).get_id();
    }


    public void updateMessage(String _id, String text, Boolean unReady, Date time) {
        mongoOperations.updateFirst(new Query(Criteria.where("_id").is(_id)),
                new Update()
                        .set("text", text)
                        .set("unReady",unReady)
                        .set("time",time),
                Message.class);
    }

    public void removeMessage(Integer userId, String messageId) {
        messageRepository.deleteBy_idAndUserId(messageId,userId);
    }


}
