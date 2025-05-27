package com.urlshortener.naataurl.controller;

import com.urlshortener.naataurl.request.*;
import com.urlshortener.naataurl.response.*;
import com.urlshortener.naataurl.utils.UrlMapperHelper;
import com.urlshortener.naataurl.utils.UserHelper;
import org.hibernate.internal.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private @Autowired UrlMapperHelper urlMapperHelper;

    private @Autowired UserHelper userHelper;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.findByUserEmail(loginRequest.getEmail());
            String password = loginRequest.getPassword();
            if(user == null){
                return ResponseEntity.badRequest().body("User is not registered");
            }
            if (StringHelper.isEmpty(password) || !passwordEncoder.matches(password, user.getUserPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ExceptionResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid Password"));
            }
            if(!user.isVerified()){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ExceptionResponse(HttpStatus.UNAUTHORIZED.value(), "User is not verified."));
            }
            String token = jwtUtils.generateToken(user);
            LoginResponse loginResponse = new LoginResponse(token, new UserResponse(user.getUserId(), user.getUserName(), user.getUserEmail()));
            
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid email or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        if (userService.findByUserEmail(registerRequest.getEmail()) != null) {
            return ResponseEntity.badRequest().body("User already signed up. Please Login or verify your email if not done already.");
        }

        User user = new User();
        user.setUserName(registerRequest.getUsername());
        user.setUserEmail(registerRequest.getEmail());
        user.setUserPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreatedAt(new Date());
        userHelper.generateAndSetUserToken(user);
        try{
            userService.sendSignupVerificationEmail(user);
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Signup Failed! Please try again.");
        }
        userService.saveUser(user);

        RegisterResponse registerResponse = new RegisterResponse("SignUp successful! Please verify your email before logging in.");
        return ResponseEntity.ok(registerResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest, Authentication authentication) {
        try {
            Long userId = null;
            try {
                userId = urlMapperHelper.getUserIdFromAuthentication(authentication);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ExceptionResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid Authentication"));
            }
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ExceptionResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid Authentication"));
            }
            String newPassword = resetPasswordRequest.getPassword();
            if(StringHelper.isEmpty(newPassword)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), "Invalid Request. Please provide a valid password"));
            }
            User user = userService.findByUserId(userId);
            user.setUserPassword(passwordEncoder.encode(newPassword));
            userService.saveUser(user);
            return ResponseEntity.ok("Password reset successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid email or password");
        }
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        try {
            String userEmail = forgotPasswordRequest.getUserEmail();
            String newPassword = forgotPasswordRequest.getPassword();
            String confirmationToken = forgotPasswordRequest.getConfirmationToken();
            if(StringHelper.isEmpty(userEmail)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), "Invalid Request. Please provide a valid email address"));
            }
            User user = userService.findByUserEmail(userEmail);
            if(user == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), "User not found!"));
            }
            if(StringHelper.isEmpty(newPassword)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), "Invalid Request. Please provide a valid password"));
            }
            if(confirmationToken==null || !confirmationToken.equals(user.getConfirmationToken())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), "Invalid operation"));
            }
            userHelper.resetConfirmationTokenDtls(user);
            user.setUserPassword(passwordEncoder.encode(newPassword));
            userService.saveUser(user);
            return ResponseEntity.ok("Password reset successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid email or password");
        }
    }

    @PostMapping("/get-reset-token")
    public ResponseEntity<?> getResetToken(@RequestBody GetResetTokenRequest getResetTokenRequest) {
        try {
            GetResetTokenResponse getResetTokenResponse = null;
            String userEmail = getResetTokenRequest.getEmail();
            if(StringHelper.isEmpty(userEmail)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), "Invalid Request. Please provide a valid email address"));
            }
            User user = userService.findByUserEmail(userEmail);
            if(user == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), "User not found!"));
            }
            if(user.isOauth2Login()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), "User logged in via OAuth2. Trying using the third party application to login."));
            }
            String existingToken = user.getConfirmationToken();
            if(existingToken != null){
                getResetTokenResponse = new GetResetTokenResponse(true);
                return ResponseEntity.ok(getResetTokenResponse);
            }
            userHelper.generateAndSetUserToken(user);
            try{
                userService.sendForgotPasswordVerificationEmail(user);
            } catch (RuntimeException e){
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Password Reset Failed! Please try again.");
            }
            userService.saveUser(user);
            getResetTokenResponse = new GetResetTokenResponse(false);
            return ResponseEntity.ok(getResetTokenResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid email or password");
        }
    }

    @PostMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestBody ValidateTokenRequest validateTokenRequest) {
        try {
            ValidateTokenResponse validateTokenResponse = null;
            String token = validateTokenRequest.getConfirmationToken();
            if(StringHelper.isEmpty(token)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), "Invalid Request."));
            }
            User user = userService.findByConfirmationToken(token);
            if(user == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), "User not found!"));
            }
            if(user.isOauth2Login()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), "User logged in via OAuth2. Trying using the third party application to login."));
            }
            boolean tokenExpired = userHelper.validateResetToken(user);
            validateTokenResponse = new ValidateTokenResponse(user.getUserEmail(), tokenExpired, user.getConfirmationToken());

            return ResponseEntity.ok(validateTokenResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid email or password");
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody ValidateTokenRequest validateTokenRequest) {
        try {
            ValidateTokenResponse validateTokenResponse = null;
            String token = validateTokenRequest.getConfirmationToken();
            if(StringHelper.isEmpty(token)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), "Invalid Request."));
            }
            User user = userService.findByConfirmationToken(token);
            if(user == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), "User not found!"));
            }
            if(user.isOauth2Login()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), "User logged in via OAuth2. Trying using the third party application to login."));
            }
            if(user.isVerified()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), "User is already verified"));
            }
            boolean tokenExpired = userHelper.validateResetToken(user);
            validateTokenResponse = new ValidateTokenResponse(user.getUserEmail(), tokenExpired, user.getConfirmationToken());

            return ResponseEntity.ok(validateTokenResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid email or password");
        }
    }
}
