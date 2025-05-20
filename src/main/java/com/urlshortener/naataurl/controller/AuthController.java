package com.urlshortener.naataurl.controller;

import com.urlshortener.naataurl.request.LoginRequest;
import com.urlshortener.naataurl.request.RegisterRequest;
import com.urlshortener.naataurl.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.urlshortener.naataurl.persistence.model.User;
import com.urlshortener.naataurl.service.UserService;
import com.urlshortener.naataurl.utils.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.findByUserEmail(loginRequest.getEmail());
            if(user == null){
                return ResponseEntity.badRequest().body("User is not registered");
            }
            String token = jwtUtils.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", new UserResponse(user.getUserId(), user.getUserName(), user.getUserEmail()));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid email or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        if (userService.findByUserEmail(registerRequest.getEmail()) != null) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = new User();
        user.setUserName(registerRequest.getUsername());
        user.setUserEmail(registerRequest.getEmail());
        user.setUserPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreatedAt(new Date());
        
        userService.saveUser(user);
        
        // Generate token for immediate login
        String token = jwtUtils.generateToken(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok("Logged out successfully");
    }
}
