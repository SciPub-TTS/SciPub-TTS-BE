package com.brotherhood.scipubtts.email.service.impl;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.email.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final String OWLREKA_LOGO_CONTENT_ID = "logo";

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Override
    public void sendVerificationEmail(String to, String verifyLink) {
        validateEmail(to);

        Context context = new Context();
        context.setVariable("verifyLink", verifyLink);
        String plainTextContent =
                "Welcome to Owlreka!\n\n" +
                "Please verify your account by opening the link below:\n" +
                verifyLink + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not create an Owlreka account, you can safely ignore this email.";

        sendHtmlEmail(
                to,
                "Verify your Owlreka account",
                plainTextContent,
                "verify-email",
                context
        );
    }

    private void sendHtmlEmail(String to,
                               String subject,
                               String plainTextContent,
                               String templateName,
                               Context context) {
        String htmlContent = templateEngine.process(templateName, context);
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainTextContent, htmlContent);
            helper.addInline(
                    OWLREKA_LOGO_CONTENT_ID,
                    new ClassPathResource("mail/logo.png"),
                    "image/png"
            );
        } catch (MessagingException exception) {
            throw new IllegalStateException("Failed to create email from template: " + templateName, exception);
        }

        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetCode(String to, String code) {
        validateEmail(to);

        Context context = new Context();
        context.setVariable("resetCode", code);
        String plainTextContent =
                "Your password reset code is: " + code + "\n\n" +
                "This code will expire in 10 minutes.\n" +
                "If you did not request this, you can ignore this email.";

        sendHtmlEmail(
                to,
                "Your Owlreka password reset code",
                plainTextContent,
                "password-reset-code",
                context
        );
    }

    private void validateEmail(String to) {
        if (to == null || !to.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_FORMAT);
        }
    }

    @Override
    public void sendAccountBannedEmail(String to) {
        validateEmail(to);

        Context context = new Context();
        context.setVariable("supportEmail", fromEmail);
        String plainTextContent = """
                Hello,

                We are writing to inform you that your Owlreka account has been temporarily suspended due to a violation of our Terms of Service or community guidelines.

                During this suspension, you will not be able to log in or access our services.

                If you believe this action was taken in error, please reply to this email or contact our support team for further assistance.

                Best regards,
                The Owlreka Team""";

        sendHtmlEmail(
                to,
                "Important Notice: Your Owlreka Account Status",
                plainTextContent,
                "account-banned",
                context
        );
    }

    @Override
    public void sendAccountUnbannedEmail(String to) {
        validateEmail(to);

        String loginUrl = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl + "login"
                : frontendBaseUrl + "/login";
        Context context = new Context();
        context.setVariable("loginUrl", loginUrl);
        String plainTextContent = """
                Hello,

                Good news! We have reviewed your account status and your Owlreka account has been successfully restored.

                You can now log in and resume using all of our services normally.

                Thank you for your patience and for being a part of the Owlreka community.

                Best regards,
                The Owlreka Team""";

        sendHtmlEmail(
                to,
                "Update: Your Owlreka Account has been Restored",
                plainTextContent,
                "account-unbanned",
                context
        );
    }
}