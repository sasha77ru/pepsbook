package ru.sasha77.spring.pepsbook.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.sasha77.spring.pepsbook.models.Answer;
import ru.sasha77.spring.pepsbook.models.Mind;
import ru.sasha77.spring.pepsbook.repositories.AnswerRepository;
import ru.sasha77.spring.pepsbook.repositories.MindRepository;

@Service
public class MindService {
    private MindRepository mindRepository;
    private AnswerRepository answerRepository;

    @Autowired
    public MindService(MindRepository mindRepository,AnswerRepository answerRepository) {
        this.mindRepository = mindRepository;
        this.answerRepository = answerRepository;
    }

    public Mind getMind(Integer id) {
        return mindRepository.findById(id).orElse(null);
    }


    public Answer getAnswer(Integer id) {
        return answerRepository.findById(id).orElse(null);
    }

    @Cacheable("minds")
    public Page<Mind> loadMinds (String subs, Integer page, Integer size) {
        Pageable pageable = page!=null? PageRequest.of(page,size):null;
        return mindRepository.findLike(subs==null?"":subs, pageable);
    }

    @CacheEvict(value = "minds",allEntries=true)
    public void saveMind(Mind mind) {
        mindRepository.save(mind);
    }

    @CacheEvict(value = "minds",allEntries=true)
    public void deleteMind(Mind mind) {
        mindRepository.delete(mind);
    }

    @CacheEvict(value = "minds",allEntries=true)
    public void saveAnswer(Answer answer) {
        answerRepository.save(answer);
    }

    @CacheEvict(value = "minds",allEntries=true)
    public void deleteAnswer(Answer answer) {
        answerRepository.delete(answer);
    }
}
