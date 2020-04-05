package ru.sasha77.spring.pepsbook.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import ru.sasha77.spring.pepsbook.models.Interlocutor;

import java.util.List;
import java.util.Optional;

public interface InterlocRepository extends MongoRepository<Interlocutor, String> {
    @Query(sort = "{ time : -1 }")
    List<Interlocutor> findByWhoseId(Integer whoseId);

    Optional<Interlocutor> getByUserIdAndWhoseId(Integer userId, Integer whoseId);
}
