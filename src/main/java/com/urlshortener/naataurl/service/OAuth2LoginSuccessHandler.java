package com.urlshortener.naataurl.service;

import com.urlshortener.naataurl.persistence.model.User;
import com.urlshortener.naataurl.utils.AuthHelper;
import com.urlshortener.naataurl.utils.JwtUtils;

import jakarta.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
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
import java.util.Set;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthHelper authHelper;

    @Value("${frontend.url}")
    private String frontendUrl;

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
                user.setOauth2Login(true);
                user.setVerified(true);
                userService.saveUser(user);
                log.info("Created new user: {} ({})", name, email);
            } else {
                log.info("User already exists: {} ({})", user.getUserName(), user.getUserEmail());
            }
            String token = jwtUtils.generateToken(user);
            log.info("Generated JWT token for user {}: {}", email, token);
            Set<ResponseCookie> cookieSet = authHelper.cookiesToBeAdded(user, token);
            for (ResponseCookie cookie : cookieSet) {
                response.addHeader("Set-Cookie", cookie.toString());
            }
            response.sendRedirect(frontendUrl);

        } catch (Exception e) {
            log.error("Error during OAuth2 login success handling", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth2 login error");
        }
    }
} 