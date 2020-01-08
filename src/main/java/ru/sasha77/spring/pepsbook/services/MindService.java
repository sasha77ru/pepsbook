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
import redis.clients.jedis.Jedis;
import ru.sasha77.spring.pepsbook.models.Answer;
import ru.sasha77.spring.pepsbook.models.Mind;
import ru.sasha77.spring.pepsbook.models.User;
import ru.sasha77.spring.pepsbook.repositories.AnswerRepository;
import ru.sasha77.spring.pepsbook.repositories.MindRepository;

import java.util.Date;

@Service("mindService")
public class MindService {
    private MindRepository mindRepository;
    private AnswerRepository answerRepository;
    private CacheManager cacheManager;
    @Value("${my.time2RetryUseRedis}") Integer time2RetryUseRedis;
    @Value("${spring.cache.redis.key-prefix}") String redisKeyPrefix;
    private boolean toCache = true;
    private boolean neverCache = false;
    private Date redisLostDate;
    private Jedis jedis;

    @Autowired
    public MindService(MindRepository mindRepository,
                       AnswerRepository answerRepository,
                       CacheManager cacheManager,
                       @Value("${spring.redis.host}") String redisHost,
                       @Value("${spring.cache.type}") String cacheType) {
        this.mindRepository = mindRepository;
        this.answerRepository = answerRepository;
        this.cacheManager = cacheManager;

        switch (cacheType) {
            case "none"     :   this.toCache = false;this.neverCache = true;break;
            case "redis"    :   jedis = new Jedis(redisHost);break;
        }
    }

    public boolean isToCache() {
        return toCache;
    }

    public void notToCache() {
        this.toCache = false;
        redisLostDate = new Date();
    }

    private void tryToCache() {
        if (toCache || neverCache) return;
        if (((new Date()).getTime() - redisLostDate.getTime()) > time2RetryUseRedis ) toCache = true;
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

    @Cacheable(value = "minds",
            key = "'minds:'+#user.id.toString()+':'+#subs+':'+#page?.toString()+':'+#size?.toString()",
            condition = "#root.target.toCache")
    public Page<Mind> loadMinds (String subs, User user, Integer page, Integer size) {
        tryToCache();
        Pageable pageable = page!=null? PageRequest.of(page,size):null;
        return mindRepository.findLike(subs==null?"":subs, user.getId(), pageable);
    }

    public void saveMind(Mind mind) {
        if (toCache) clearUsersAndMatesCache(mind.getUser());
        mindRepository.save(mind);
    }

    public void deleteMind(Mind mind) {
        if (toCache) clearUsersAndMatesCache(mind.getUser());
        mindRepository.delete(mind);
    }

    public void saveAnswer(Answer answer) {
        if (toCache) clearUsersAndMatesCache(answer.getMind().getUser());
        answerRepository.save(answer);
    }

    public void deleteAnswer(Answer answer) {
        if (toCache) clearUsersAndMatesCache(answer.getMind().getUser());
        answerRepository.delete(answer);
    }

//    @CacheEvict(value = "minds",allEntries=true, condition = "#root.target.toCache")
    public void clearCache() {
        if (toCache) cacheManager.getCache("minds").clear();
    }

    public void clearCache(User user) {
        if (!toCache) return;
        try {
            if (jedis!=null) {
                jedis.eval("return redis.call('DEL', unpack(redis.call('KEYS', ARGV[1])))",0,
                        redisKeyPrefix+"minds:"+user.getId()+":*");
            } else clearCache();
        } catch (Exception ignored) {}
    }

    public void clearUsersAndMatesCache(User user) {
        user.getMates().forEach(this::clearCache);
        clearCache(user);
    }
}
