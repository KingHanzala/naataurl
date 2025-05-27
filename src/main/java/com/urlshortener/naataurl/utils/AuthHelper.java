package com.urlshortener.naataurl.utils;

import com.urlshortener.naataurl.persistence.model.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class AuthHelper {

    private static final Logger log = LoggerFactory.getLogger(AuthHelper.class);

    @Value("${app.name}")
    private String appName;

    @Value("${deployment.mode}")
    private String deploymentMode;

    @Value("${same.site.policy}")
    private String sameSite;

    @Autowired
    private UserHelper userHelper;

    public static final String delim = "_";
    public final String JWT = "jwt";
    public final String DEVICE = "device";

    public ResponseCookie setCookie(String type, String value){
        String cookieType = appName + delim + type;
        return ResponseCookie.from(cookieType, value)
                .httpOnly(true)
                .secure("production".equals(deploymentMode))
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite(sameSite)
                .build();
    }

    public ResponseCookie deleteCookie(String type){
        String cookieType = appName + delim + type;
        return ResponseCookie.from(cookieType, "")
                .httpOnly(true)
                .secure("production".equals(deploymentMode))
                .path("/")
                .maxAge(0)
                .sameSite(sameSite)
                .build();
    }

    public Set<ResponseCookie> cookiesToBeAdded(User user, String token) {
        log.info("Generated JWT token for user {}: {}", user.getUserEmail(), token);
        ResponseCookie authCookie = setCookie(JWT,token);
        String uuid = userHelper.generateRandomToken();
        ResponseCookie deviceCookie = setCookie(DEVICE,uuid);
        Set<ResponseCookie> cookieSet= new HashSet<>();
        cookieSet.add(authCookie);
        cookieSet.add(deviceCookie);
        return cookieSet;
    }

    public Set<ResponseCookie> clearCookies(){
        ResponseCookie authCookie = deleteCookie(JWT);
        ResponseCookie deviceCookie = deleteCookie(DEVICE);
        Set<ResponseCookie> cookieSet= new HashSet<>();
        cookieSet.add(authCookie);
        cookieSet.add(deviceCookie);
        return cookieSet;
    }

    public String getJwtFromCookies(HttpServletRequest request) {
        for (Cookie cookie : request.getCookies()) {
            if ("jwt".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
