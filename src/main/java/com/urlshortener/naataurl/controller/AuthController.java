package com.urlshortener.naataurl.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
public class AuthController {
    private static final String COUNT = "COUNT";

    @GetMapping("/")
    public String home(Principal principal, HttpSession session) {
        incrementCount(session);
        return "Welcome to the URL Shortener API, " + principal.getName();
    }

    private void incrementCount(HttpSession session) {
        Integer count = (Integer) session.getAttribute(COUNT);
        if (count == null) {
            count = 0;
        }
        count++;
        session.setAttribute(COUNT, count);
    }
    @GetMapping("/count")
    public String getCount(HttpSession session) {
        Integer count = (Integer) session.getAttribute(COUNT);
        if (count == null) {
            return "No requests made yet.";
        }
        return "Number of requests made: " + count;
    }
}
