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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private static final String EMAIL_TEMPLATE_PATH = "templates/email/email-template.html";
    private static final String FORGOT_PASSWORD_CONTENT_PATH = "templates/email/content/forgot-password.html";
    private static final String SIGNUP_VERIFICATION_CONTENT_PATH = "templates/email/content/signup-verification.html";
    private static final String LOGO_PATH = "static/images/logo.png";

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String subject, String body) throws MessagingException {
        logger.debug("Attempting to send verification email to: {}", to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            logger.info("Successfully sent verification email to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send verification email to: {}. Error: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    public void sendForgotPasswordEmail(String to, String url) throws MessagingException {
        logger.debug("Attempting to send forgot password email to: {}", to);
        try {
            String content = loadContent(FORGOT_PASSWORD_CONTENT_PATH).replace("${url}", url);
            sendHtmlEmail(to, "Reset Your Password - Cryptoutils", content);
            logger.info("Successfully sent forgot password email to: {}", to);
        } catch (IOException e) {
            logger.error("Failed to load forgot password email content. Error: {}", e.getMessage(), e);
            throw new MessagingException("Failed to load forgot password email content", e);
        } catch (Exception e) {
            logger.error("Failed to send forgot password email to: {}. Error: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    public void sendSignupVerificationEmail(String to, String url) throws MessagingException {
        logger.debug("Attempting to send signup verification email to: {}", to);
        try {
            String content = loadContent(SIGNUP_VERIFICATION_CONTENT_PATH).replace("${url}", url);
            sendHtmlEmail(to, "Verify Your Email - Cryptoutils", content);
            logger.info("Successfully sent signup verification email to: {}", to);
        } catch (IOException e) {
            logger.error("Failed to load signup verification email content. Error: {}", e.getMessage(), e);
            throw new MessagingException("Failed to load signup verification email content", e);
        } catch (Exception e) {
            logger.error("Failed to send signup verification email to: {}. Error: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    private void sendHtmlEmail(String to, String subject, String content) throws MessagingException {
        logger.debug("Attempting to send HTML email to: {}", to);
        try {
            String template = loadTemplate();
            String htmlContent = template
                .replace("${subject}", subject)
                .replace("${content}", content)
                .replace("${year}", String.valueOf(Year.now().getValue()));

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // Add logo as embedded image
            ClassPathResource logoResource = new ClassPathResource(LOGO_PATH);
            if (logoResource.exists()) {
                helper.addInline("logo", logoResource);
            } else {
                logger.warn("Logo image not found at path: {}", LOGO_PATH);
            }

            mailSender.send(message);
            logger.info("Successfully sent HTML email to: {}", to);
        } catch (IOException e) {
            logger.error("Failed to load email template. Error: {}", e.getMessage(), e);
            throw new MessagingException("Failed to send HTML email", e);
        } catch (Exception e) {
            logger.error("Failed to send HTML email to: {}. Error: {}", to, e.getMessage(), e);
            throw e;
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

