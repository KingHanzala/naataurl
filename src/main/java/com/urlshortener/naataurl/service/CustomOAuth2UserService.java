package com.urlshortener.naataurl.service;

import com.urlshortener.naataurl.persistence.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Enum for OAuth2 providers
enum OAuthProvider {
    GOOGLE, GITHUB, TWITTER, OTHER;
    public static OAuthProvider fromRegistrationId(String registrationId) {
        if (registrationId == null) return OTHER;
        switch (registrationId.toLowerCase()) {
            case "google": return GOOGLE;
            case "github": return GITHUB;
            case "twitter":
            case "x": return TWITTER;
            default: return OTHER;
        }
    }
}

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @SuppressWarnings("unchecked")
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = new java.util.HashMap<>(oAuth2User.getAttributes());
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuthProvider provider = OAuthProvider.fromRegistrationId(registrationId);
        log.info("OAuth2 login attempt. Provider: {}", provider);
        String email = null;
        String username = null;
        switch (provider) {
            case GOOGLE:
                email = (String) attributes.get("email");
                username = (String) attributes.getOrDefault("name", email);
                log.info("Google OAuth2: email={}, username={}", email, username);
                break;
            case GITHUB:
                email = (String) attributes.get("email");
                username = (String) attributes.getOrDefault("name", email);
                log.info("GitHub OAuth2: email={}, username={}", email, username);
                if (email == null) {
                    log.warn("Email not found in main attributes. Attempting to fetch from /user/emails endpoint.");
                    String token = userRequest.getAccessToken().getTokenValue();
                    RestTemplate restTemplate = new RestTemplate();
                    try {
                        var headers = new org.springframework.http.HttpHeaders();
                        headers.add("Authorization", "Bearer " + token);
                        headers.add("Accept", "application/vnd.github.v3+json");
                        var entity = new org.springframework.http.HttpEntity<>(headers);
                        var response = restTemplate.exchange(
                                "https://api.github.com/user/emails",
                                org.springframework.http.HttpMethod.GET,
                                entity,
                                List.class
                        );
                        List<Map<String, Object>> emails = response.getBody();
                        if (emails != null) {
                            for (Map<String, Object> mail : emails) {
                                if (Boolean.TRUE.equals(mail.get("primary")) && Boolean.TRUE.equals(mail.get("verified"))) {
                                    email = (String) mail.get("email");
                                    log.info("Fetched primary verified email from /user/emails: {}", email);
                                    attributes.put("email", email);
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Failed to fetch email from /user/emails endpoint", e);
                    }
                }
                break;
            case TWITTER:
                email = (String) attributes.get("email");
                username = (String) attributes.getOrDefault("name", attributes.get("screen_name"));
                log.info("Twitter/X OAuth2: email={}, username={}", email, username);
                break;
            case OTHER:
            default:
                email = (String) attributes.get("email");
                username = (String) attributes.getOrDefault("name", email);
                log.info("Other OAuth2 provider ({}): email={}, username={}", registrationId, email, username);
                break;
        }
        if (email == null) {
            log.error("Email not found from OAuth2 provider after all attempts. Provider: {}", provider);
            throw new RuntimeException("Email not found from OAuth2 provider");
        }
        User user = userService.findByUserEmail(email);
        if (user == null) {
            user = new User();
            user.setUserName(username);
            user.setUserEmail(email);
            user.setUserPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setCreatedAt(new Date());
            user.setOauth2Login(true);
            user.setVerified(true);
            userService.saveUser(user);
            log.info("Created new user: {} ({})", username, email);
        } else {
            log.info("User already exists: {} ({})", user.getUserName(), user.getUserEmail());
        }
        return new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes, "email");
    }
} 