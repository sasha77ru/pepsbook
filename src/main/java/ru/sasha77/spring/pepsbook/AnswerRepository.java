package ru.sasha77.spring.pepsbook;

import org.springframework.data.repository.CrudRepository;

public interface AnswerRepository extends CrudRepository<Answer, Integer> {
    Answer findByText(String text);
}
