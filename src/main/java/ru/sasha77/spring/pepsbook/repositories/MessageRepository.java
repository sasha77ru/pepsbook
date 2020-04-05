package ru.sasha77.spring.pepsbook.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import ru.sasha77.spring.pepsbook.models.Message;

public interface MessageRepository extends MongoRepository<Message, String> {
    @Query(value = "{ $or : [{userId : ?0,whomId : ?1},{userId : ?1,whomId : ?0}]}",
            sort = "{ time : -1 }")
    Page<Message> findLike(Integer userId, Integer whomId, Pageable pageable);

    Message findByText(String text);

    void deleteBy_idAndUserId(String _id, Integer userId);
}
