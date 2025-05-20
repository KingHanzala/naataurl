package com.urlshortener.naataurl.service;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.urlshortener.naataurl.persistence.model.User;
import com.urlshortener.naataurl.persistence.repository.UserRepository;

@Service
public class UserService {
    
    private @Autowired UserRepository userRepository;

    private @Autowired EmailService emailService;

    @Value("${frontend.url}")
    private String frontendUrl;
    
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

    public void sendSignupVerificationEmail(User user) throws RuntimeException{
        try{
            String URL = frontendUrl + "/confirm?token="+user.getConfirmationToken();
            emailService.sendSignupVerificationEmail(user.getUserEmail(), URL);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendForgotPasswordVerificationEmail(User user) throws RuntimeException{
        try{
            String URL = frontendUrl + "/confirm?token="+user.getConfirmationToken();
            emailService.sendForgotPasswordEmail(user.getUserEmail(), URL);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
