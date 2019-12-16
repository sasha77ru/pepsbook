package ru.sasha77.spring.pepsbook.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.sasha77.spring.pepsbook.models.User;
import ru.sasha77.spring.pepsbook.repositories.UserRepository;

@Service("MyUserService")
public class UserService implements UserDetailsService {
    private UserRepository userRepo;
    @Autowired
    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);
        if (user != null) return user;
        throw new UsernameNotFoundException("User '" + username + "' not found");
    }

    @CacheEvict(value = "minds",allEntries=true)
    public void deleteAll() {
        userRepo.deleteAll();
    }
}
