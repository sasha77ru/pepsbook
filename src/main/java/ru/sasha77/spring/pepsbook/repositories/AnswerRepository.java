package ru.sasha77.spring.pepsbook.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.sasha77.spring.pepsbook.models.Answer;

public interface AnswerRepository extends CrudRepository<Answer, Integer> {
    Answer findByText(String text);
}
