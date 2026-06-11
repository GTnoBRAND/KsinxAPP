package org.jas.ksinxapp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Year;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine springTemplateEngine;
    private final String from;
    private final String fromName;
    private final String baseUrl;
    private final int expiryHours;


    public EmailService(JavaMailSender mailSender,
                        SpringTemplateEngine springTemplateEngine,
                        @Value("${app.mail.from}") String from,
                        @Value("${app.mail.from-name}") String fromName,
                        @Value("${app.base-url}") String baseUrl,
                        @Value("${app.mail.verification.expiry-hours:24}") int expiryHours) {
        this.mailSender = mailSender;
        this.springTemplateEngine = springTemplateEngine;
        this.from = from;
        this.fromName = fromName;
        this.baseUrl = baseUrl;
        this.expiryHours = expiryHours;
    }

    public void sendVerificationEmail(String to, String recipientName, String token){
        String link = baseUrl + "/verify-email?token=" + token;

        Context context = new Context();
        context.setVariable("recipientName", recipientName != null ? recipientName : "there");
        context.setVariable("verificationLink", link);
        context.setVariable("expiryHours", expiryHours);
        context.setVariable("year", Year.now().getValue());

        String htmlBody = springTemplateEngine.process("verification.html", context);
        String textBody = springTemplateEngine.process("verification.txt", context);

        sendMime(to, from, htmlBody, textBody);
    }

    @Async
    public void sendAccountExistingEmail(String to){
        Context context = new Context();
        context.setVariable("loginLink", baseUrl + "/login");
        context.setVariable("passwordResetLink", baseUrl +"/auth/forgot-password");
        context.setVariable("year", Year.now().getValue());

        String htmlBody = springTemplateEngine.process("verification.html", context);
        String textBody = springTemplateEngine.process("verification.txt", context);

        sendMime(to, from, htmlBody, textBody);
    }

    private void sendMime(String to, String subject, String htmlBody, String textBody){
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(new InternetAddress(from, fromName, StandardCharsets.UTF_8.name()));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textBody, htmlBody);

            mailSender.send(message);
            log.info("Email '{}' sent to {}", subject, to);
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            log.info("Failed to send '{}' to {}", subject, to);
            throw new RuntimeException(e);
        }
    }

}
