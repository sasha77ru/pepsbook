package ru.sasha77.spring.pepsbook.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import ru.sasha77.spring.pepsbook.models.Interlocutor;
import ru.sasha77.spring.pepsbook.models.User;
import ru.sasha77.spring.pepsbook.repositories.InterlocRepository;
import ru.sasha77.spring.pepsbook.repositories.UserRepository;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class InterlocService {
    private UserRepository userRepository;
    private InterlocRepository interlocRepository;
    private MongoOperations mongoOperations;
    @Autowired
    public InterlocService(UserRepository userRepository,
                           InterlocRepository interlocRepository,
                           MongoOperations mongoOperations) {
        this.userRepository = userRepository;
        this.interlocRepository = interlocRepository;
        this.mongoOperations = mongoOperations;
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
     * @param userId interlocutor
     * @param whoseId whose
     */
    public void incNumNewMessages (Integer userId, Integer whoseId) {
        mongoOperations.updateFirst(new Query(Criteria.where("userId").is(userId).and("whoseId").is(whoseId)),
                new Update()
                        .inc("numNewMessages", 1)
                        .set("hasPreMessages",false),
                Interlocutor.class);
    }


    /**
     * Clear numNewMessages and hasPreMessages in MongoDB
     * @param userId interlocutor
     * @param whoseId whose
     */
    public void clearInterlocutorState (Integer userId, Integer whoseId) {
        mongoOperations.updateFirst(new Query(Criteria.where("userId").is(userId).and("whoseId").is(whoseId)),
                new Update()
                        .set("numNewMessages", 0)
                        .set("hasPreMessages",false),
                Interlocutor.class);
    }
}
