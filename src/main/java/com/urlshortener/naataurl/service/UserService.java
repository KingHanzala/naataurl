package com.urlshortener.naataurl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.urlshortener.naataurl.persistence.model.User;
import com.urlshortener.naataurl.persistence.repository.UserRepository;

@Service
public class UserService {
    
    private @Autowired UserRepository userRepository;
    
    public User findByUserName(String username) {
        return userRepository.findByUserName(username);
    }

    public User findByUserId(Long id) {
        return userRepository.findByUserId(id);
    }
    public User findByUserEmail(String email) {
        return userRepository.findByUserEmail(email);
    }

    public User findByConfirmationToken(String confirmationToken){
        return userRepository.findByConfirmationToken(confirmationToken);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

}
