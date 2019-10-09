package ru.sasha77.spring.pepsbook;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AnswerRepository extends CrudRepository<Answer, Integer> {
    Answer findByText(String text);
}
