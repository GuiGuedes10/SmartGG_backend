package com.smartgg.smartgg_backend.services;

import org.springframework.stereotype.Service;
import com.smartgg.smartgg_backend.model.User;
import com.smartgg.smartgg_backend.repositories.UserRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(User user) {
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
