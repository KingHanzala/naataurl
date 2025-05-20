package com.urlshortener.naataurl.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.io.IOException;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private static final String EMAIL_TEMPLATE_PATH = "templates/email/email-template.html";
    private static final String FORGOT_PASSWORD_CONTENT_PATH = "templates/email/content/forgot-password.html";
    private static final String SIGNUP_VERIFICATION_CONTENT_PATH = "templates/email/content/signup-verification.html";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true); // true enables HTML

        mailSender.send(message);
    }

    public void sendForgotPasswordEmail(String to, String url) throws MessagingException {
        try {
            String content = loadContent(FORGOT_PASSWORD_CONTENT_PATH).replace("${url}", url);
            sendHtmlEmail(to, "Reset Your Password - NaataURL", content);
        } catch (IOException e) {
            throw new MessagingException("Failed to load forgot password email content", e);
        }
    }

    public void sendSignupVerificationEmail(String to, String url) throws MessagingException {
        try {
            String content = loadContent(SIGNUP_VERIFICATION_CONTENT_PATH).replace("${url}", url);
            sendHtmlEmail(to, "Verify Your Email - NaataURL", content);
        } catch (IOException e) {
            throw new MessagingException("Failed to load signup verification email content", e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String content) throws MessagingException {
        try {
            String template = loadTemplate();
            String htmlContent = template
                .replace("${subject}", subject)
                .replace("${content}", content)
                .replace("${year}", String.valueOf(Year.now().getValue()));

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (IOException e) {
            throw new MessagingException("Failed to send HTML email", e);
        }
    }

    private String loadTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource(EMAIL_TEMPLATE_PATH);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }

    private String loadContent(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}

