package com.urlshortener.naataurl.controller;


import com.urlshortener.naataurl.manager.RedisManager;
import com.urlshortener.naataurl.manager.UrlManager;
import com.urlshortener.naataurl.response.*;
import com.urlshortener.naataurl.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

import com.urlshortener.naataurl.utils.UrlMapperHelper;
import com.urlshortener.naataurl.service.UserService;
import com.urlshortener.naataurl.service.UrlService;
import com.urlshortener.naataurl.persistence.model.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    private @Autowired UrlMapperHelper urlMapperHelper;
    private @Autowired UserService userService;
    private @Autowired RedisManager redisManager;
    private @Autowired UrlManager urlManager;

    @GetMapping("/dashboard")
    ResponseEntity<?> getUserDashboard(Authentication authentication) {
        Long userId = null;
        try {
            userId = urlMapperHelper.getUserIdFromAuthentication(authentication);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ExceptionResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid Authentication"));
        }
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ExceptionResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid Authentication"));
        }

        // Get user information
        User user = userService.findByUserId(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionResponse(HttpStatus.NOT_FOUND.value(), "User not found"));
        }

        // Get user's URLs
        GetUserDashboardResponse getUserDashboardResponse = null;
        getUserDashboardResponse = redisManager.getUserDashboardResponse(String.valueOf(userId));
        if(getUserDashboardResponse == null){
            getUserDashboardResponse = urlManager.getUserDashboardResponse(userId);
            redisManager.saveUserDashboard(String.valueOf(userId), getUserDashboardResponse);
        }

        return ResponseEntity.ok(getUserDashboardResponse);
    }
}
