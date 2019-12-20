package ru.sasha77.spring.pepsbook.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
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

import java.util.Date;

@Service("mindService")
public class MindService {
    private MindRepository mindRepository;
    private AnswerRepository answerRepository;
    private CacheManager cacheManager;
    @Value("${my.time2RetryUseRedis}") Integer time2RetryUseRedis;
    private boolean toCache = true;
    private Date redisLostDate;

    @Autowired
    public MindService(MindRepository mindRepository,AnswerRepository answerRepository,CacheManager cacheManager) {
        this.mindRepository = mindRepository;
        this.answerRepository = answerRepository;
        this.cacheManager = cacheManager;
    }

    public boolean isToCache() {
        return toCache;
    }

    public void notToCache() {
        this.toCache = false;
        redisLostDate = new Date();
    }

    private void tryToCache() {
        if (!toCache && ((new Date()).getTime() - redisLostDate.getTime()) > time2RetryUseRedis ) toCache = true;
        try {
            cacheManager.getCache("minds").clear();
        } catch (Exception e) {
            notToCache();
        }
    }

    public Mind getMind(Integer id) {
        return mindRepository.findById(id).orElse(null);
    }

    public Answer getAnswer(Integer id) {
        return answerRepository.findById(id).orElse(null);
    }

    @Cacheable(value = "minds", condition = "#root.target.toCache")
    public Page<Mind> loadMinds (String subs, Integer page, Integer size) {
        tryToCache();
        Pageable pageable = page!=null? PageRequest.of(page,size):null;
        return mindRepository.findLike(subs==null?"":subs, pageable);
    }

    @CacheEvict(value = "minds",allEntries=true, condition = "#root.target.toCache")
    public void saveMind(Mind mind) {
        mindRepository.save(mind);
    }

    @CacheEvict(value = "minds",allEntries=true, condition = "#root.target.toCache")
    public void deleteMind(Mind mind) {
        mindRepository.delete(mind);
    }

    @CacheEvict(value = "minds",allEntries=true, condition = "#root.target.toCache")
    public void saveAnswer(Answer answer) {
        answerRepository.save(answer);
    }

    @CacheEvict(value = "minds",allEntries=true, condition = "#root.target.toCache")
    public void deleteAnswer(Answer answer) {
        answerRepository.delete(answer);
    }
}
