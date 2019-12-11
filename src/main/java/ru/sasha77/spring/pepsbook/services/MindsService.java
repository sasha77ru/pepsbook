package ru.sasha77.spring.pepsbook.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.sasha77.spring.pepsbook.models.Mind;
import ru.sasha77.spring.pepsbook.repositories.MindRepository;

@Service
public class MindsService {
    private MindRepository mindRepository;

    public MindsService(@Autowired MindRepository mindRepository) {
        this.mindRepository = mindRepository;
    }

    public Page<Mind> loadMinds (String subs, Integer page, Integer size) {
        Pageable pageable = page!=null? PageRequest.of(page,size):null;
        return mindRepository.findLike(subs==null?"":subs, pageable);
    }

}
