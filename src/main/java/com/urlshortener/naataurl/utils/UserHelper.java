package com.urlshortener.naataurl.utils;

import com.urlshortener.naataurl.persistence.model.User;
import com.urlshortener.naataurl.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
public class UserHelper {
    private static final Logger logger = LoggerFactory.getLogger(UserHelper.class);

    private @Autowired UserService userService;
    public String generateRandomToken(){
        return UUID.randomUUID().toString();
    }

    public void generateAndSetUserToken(User user){
        String token = generateRandomToken();
        user.setConfirmationToken(token);
        Instant expiryInstant = Instant.now().plus(48, ChronoUnit.HOURS);
        user.setTokenExpiry(Date.from(expiryInstant));
        userService.saveUser(user);
    }

    public boolean validateResetToken(User user){
        if(user.getTokenExpiry()!=null && user.getTokenExpiry().before(new Date())) {
            logger.info("Password Reset Token expired for user {}", user.getUserEmail());
            generateAndSetUserToken(user);
            return true;
        }
        //Signup Flow
        if(!user.isVerified()){
            resetConfirmationTokenDtls(user);
            user.setVerified(true);
        }
        userService.saveUser(user);
        return false;
    }

    public void resetConfirmationTokenDtls(User user){
        user.setConfirmationToken(null);
        user.setTokenExpiry(null);
    }
}
