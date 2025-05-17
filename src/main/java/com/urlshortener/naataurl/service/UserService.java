package com.urlshortener.naataurl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.urlshortener.naataurl.persistence.model.User;
import com.urlshortener.naataurl.persistence.repository.UserRepository;

@Service
public class UserService {
    
    private @Autowired UserRepository userRepository;
    
    public User findByUserName(String username) {
        User user = userRepository.findByUserName(username);
        return user;
    }

    public User findByUserId(Long id) {
        User user = userRepository.findByUserId(id);
        return user;
    }
    public User findByUserEmail(String email) {
        User user = userRepository.findByUserEmail(email);
        return user;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

}
