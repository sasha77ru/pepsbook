package ru.sasha77.spring.pepsbook.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.sasha77.spring.pepsbook.models.Interlocutor;
import ru.sasha77.spring.pepsbook.models.User;
import ru.sasha77.spring.pepsbook.repositories.InterlocRepository;
import ru.sasha77.spring.pepsbook.repositories.UserRepository;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static ru.sasha77.spring.pepsbook.config.WebSocketConfig.MESSAGE_PREFIX;

@Service
public class InterlocService {
    private final UserRepository userRepository;
    private final InterlocRepository interlocRepository;
    private final MongoOperations mongoOperations;
    private final SimpMessagingTemplate websocket;
    @Autowired
    public InterlocService(UserRepository userRepository,
                           InterlocRepository interlocRepository,
                           MongoOperations mongoOperations,
                           SimpMessagingTemplate websocket) {
        this.userRepository = userRepository;
        this.interlocRepository = interlocRepository;
        this.mongoOperations = mongoOperations;
        this.websocket = websocket;
    }

    public List<Interlocutor> loadInterlocutors (Integer whoseId) {
        // TODO: 21.03.2020 findLike with subs
        return interlocRepository.findByWhoseId(whoseId);
    }

    /**
     * Adds users as interlocutor to each other. If doesn't exist yet
     * @param user first user
     * @param interlocutor second user
     */
    public void newInterlocutors(User user, User interlocutor) {
        Arrays.asList(Arrays.asList(user,interlocutor),Arrays.asList(interlocutor,user)).forEach(it -> {
            User first = it.get(0);User second = it.get(1);
            Interlocutor interlocutor2save = interlocRepository.getByUserIdAndWhoseId(first.getId(),second.getId())
                    .orElse(new Interlocutor(
                            first.getName(),
                            first.getId(),
                            second.getName(),
                            second.getId(),
                            0,
                            false,
                            new Date()
                    ));
            interlocutor2save.setTime(new Date());
            interlocRepository.save(interlocutor2save);
        });
    }

    /**
     * Increase numNewMessages and clear hasPreMessages in MongoDB
     * @param whoseId whose
     * @param userId interlocutor
     */
    public void incNumNewMessages(Integer whoseId, Integer userId, String whoseName) {
        mongoOperations.updateFirst(new Query(Criteria.where("userId").is(userId).and("whoseId").is(whoseId)),
                new Update()
                        .inc("numNewMessages", 1)
                        .set("hasPreMessages",false),
                Interlocutor.class);
        try {
            JSONObject json = new JSONObject();
            json.put("userId",userId);
            json.put("whoseId",whoseId);
            this.websocket.convertAndSend(MESSAGE_PREFIX + "/updateInterlocutors/"+whoseId, json.toString());
        } catch (JSONException ignored) {}
    }

    /**
     * Set hasPreMessages
     * @param whoseId whose
     * @param userId interlocutor
     */
    public void setHasPreMessages(Integer whoseId, Integer userId, String whoseName) {
        mongoOperations.updateFirst(new Query(Criteria.where("userId").is(userId).and("whoseId").is(whoseId)),
                new Update()
                        .set("hasPreMessages",true),
                Interlocutor.class);
        try {
            JSONObject json = new JSONObject();
            json.put("userId",userId);
            json.put("whoseId",whoseId);
            this.websocket.convertAndSend(MESSAGE_PREFIX + "/updateInterlocutors/"+whoseId, json.toString());
        } catch (JSONException ignored) {}
    }

    /**
     * Clear numNewMessages and hasPreMessages in MongoDB
     * @param whoseId whose
     * @param userId interlocutor
     */
    public void clearInterlocutorState(Integer whoseId, Integer userId) {
        Interlocutor interlocutor = interlocRepository.getByUserIdAndWhoseId(userId,whoseId).orElseThrow(() -> new NullPointerException("No such Interlocutor"));
        if (interlocutor.getNumNewMessages() > 0 || interlocutor.getHasPreMessages()) {
            // to not send ws if it's not necessary. Otherwise - infinity loop
            interlocutor.setNumNewMessages(0);
            interlocutor.setHasPreMessages(false);
            interlocRepository.save(interlocutor);
            try {
                JSONObject json = new JSONObject();
                json.put("userId", userId);
                json.put("whoseId", whoseId);
                this.websocket.convertAndSend(MESSAGE_PREFIX + "/updateInterlocutors/" + whoseId, json.toString());
            } catch (JSONException ignored) {}
        }
    }
}
