package com.urlshortener.naataurl.service;

import com.urlshortener.naataurl.persistence.model.User;
import com.urlshortener.naataurl.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;

    private @Autowired PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = (String) oAuth2User.getAttributes().get("email");
            String name = (String) oAuth2User.getAttributes().get("name");
            log.info("OAuth2 authentication success for email: {} with name {}", email, name);
            User user = userService.findByUserEmail(email);
            if (user == null) {
                user = new User();
                user.setUserName(name);
                user.setUserEmail(email);
                user.setUserPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setCreatedAt(new Date());
                userService.saveUser(user);
                log.info("Created new user: {} ({})", name, email);
            } else {
                log.info("User already exists: {} ({})", user.getUserName(), user.getUserEmail());
            }
            String token = jwtUtils.generateToken(user);
            log.info("Generated JWT token for user {}: {}", email, token);
            // Redirect to frontend with token
            response.sendRedirect("http://localhost:3000/successCallback?token=" + token);

        } catch (Exception e) {
            log.error("Error during OAuth2 login success handling", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth2 login error");
        }
    }
} 