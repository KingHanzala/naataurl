package com.urlshortener.naataurl.controller;


import com.urlshortener.naataurl.response.*;
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
    private @Autowired UrlService urlService;

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
        List<GetUrlInfoResponse> urls = urlService.findByUserId(userId).stream()
            .map(urlMapper -> {
                GetUrlInfoResponse urlInfo = new GetUrlInfoResponse();
                urlInfo.setOriginalUrl(urlMapper.getOriginalUrl());
                urlInfo.setShortUrl(urlMapper.getShortUrl());
                urlInfo.setUrlClicks(urlMapper.getUrlClicks());
                urlInfo.setCreatedDtm(urlMapper.getCreatedAt());
                urlInfo.setUserId(urlMapper.getUserId());
                return urlInfo;
            })
            .collect(Collectors.toList());

        // Create response
        GetUserDashboardResponse response = new GetUserDashboardResponse();
        response.setUserResponse(new UserResponse(user.getUserId(), user.getUserName(), user.getUserEmail()));
        response.setUrlsMappedList(urls);
        response.setAvailableCredits(user.getUsageCredits());

        return ResponseEntity.ok(response);
    }
}
