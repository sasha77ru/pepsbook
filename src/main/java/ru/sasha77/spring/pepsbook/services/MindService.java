package ru.sasha77.spring.pepsbook.services;

import net.sf.ehcache.Ehcache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
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
import java.util.Objects;

@Service("mindService")
public class MindService {
    private MindRepository mindRepository;
    private AnswerRepository answerRepository;
    private CacheManager cacheManager;
    private Ehcache ehcache;
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

        // Depending on spring.cache.type set variables
        try {
            switch (cacheType) {
                case "none"     :   throw new RuntimeException();
                case "redis"    :   jedis = new Jedis(redisHost);break;
                case "ehcache"  :   ehcache = (Ehcache) Objects.requireNonNull(cacheManager.getCache("minds")).getNativeCache();break;
            }
        } catch (Exception ignored) {
            this.toCache = false;this.neverCache = true;
        }
    }

    public boolean isToCache() {
        return toCache;
    }

    /**
     * Disable caching and remember time to repeat attempts to connect
     */
    public void notToCache() {
        this.toCache = false;
        redisLostDate = new Date();
    }

    /**
     * If time2RetryUseRedis expired, tries to use cache: Clear it.
     * If no success - disables cache by the next term.
     */
    private void tryToCache() {
        if (toCache || neverCache) return;
        if (((new Date()).getTime() - redisLostDate.getTime()) > time2RetryUseRedis ) toCache = true;
        try {
            Objects.requireNonNull(cacheManager.getCache("minds")).clear();
        } catch (Exception e) {
            notToCache();
        }
    }

    /**
     * Finds a mind by id
     * @param id
     * @return
     */
    public Mind getMind(Integer id) {
        return mindRepository.findById(id).orElse(null);
    }

    /**
     * Finds an answer by id
     * @param id
     * @return
     */
    public Answer getAnswer(Integer id) {
        return answerRepository.findById(id).orElse(null);
    }

    /**
     * Returns page of minds. Supports caching.
     * @param subs Substring to match in mind text or answer of mind text or author name
     * @param user Current user (to know his friends, whose minds should be got)
     * @param page Number of page. Null if all
     * @param size Page size
     * @return Page object with minds
     */
    @Cacheable(value = "minds",
            key = "'minds:'+#user.id.toString()+':'+#subs+':'+#page?.toString()+':'+#size?.toString()",
            condition = "#root.target.toCache")
    public Page<Mind> loadMinds (String subs, User user, Integer page, Integer size) {
        tryToCache();
        Pageable pageable = page!=null? PageRequest.of(page,size):null;
        return mindRepository.findLike(subs==null?"":subs, user.getId(), pageable);
    }

    /**
     * Saves the mind, evicting minds cache for user and his mates.
     * @param mind
     */
    public void saveMind(Mind mind) {
        if (toCache) clearUsersAndMatesCache(mind.getUser());
        mindRepository.save(mind);
    }

    /**
     * Deletes the mind, evicting minds cache for user and his mates.
     * @param mind
     */
    public void deleteMind(Mind mind) {
        if (toCache) clearUsersAndMatesCache(mind.getUser());
        mindRepository.delete(mind);
    }

    /**
     * Saves the answer, evicting minds cache for user and his mates.
     * @param answer
     */
    public void saveAnswer(Answer answer) {
        if (toCache) clearUsersAndMatesCache(answer.getMind().getUser());
        answerRepository.save(answer);
    }

    /**
     * Deletesthe answer, evicting minds cache for user and his mates.
     * @param answer
     */
    public void deleteAnswer(Answer answer) {
        if (toCache) clearUsersAndMatesCache(answer.getMind().getUser());
        answerRepository.delete(answer);
    }

    /**
     * Clears overall cache
     */
//    @CacheEvict(value = "minds",allEntries=true, condition = "#root.target.toCache")
    public void clearCache() {
        if (toCache) cacheManager.getCache("minds").clear();
    }

    /**
     * Evicts cache for the user: all pages and subs
     * @param user
     */
    public void clearCache(User user) {
        if (!toCache) return;
        try {
            if (ehcache!=null) {
                for (Object key: ehcache.getKeys()) {
                    if (key.toString().startsWith("minds:"+user.getId()+":")) cacheManager.getCache("minds").evict(key);
                }
            } else if (jedis!=null) {
                jedis.eval("return redis.call('DEL', unpack(redis.call('KEYS', ARGV[1])))",0,
                        redisKeyPrefix+"minds:"+user.getId()+":*");
            } else clearCache();
        } catch (Exception ignored) {}
    }

    /**
     * Runs clearCache(User user) for the user and his mates
     * @param user
     */
    public void clearUsersAndMatesCache(User user) {
        user.getMates().forEach(this::clearCache);
        clearCache(user);
    }
}
