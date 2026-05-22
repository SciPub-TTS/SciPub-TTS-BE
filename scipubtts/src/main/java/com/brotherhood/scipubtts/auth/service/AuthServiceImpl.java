package com.brotherhood.scipubtts.auth.service;

import com.brotherhood.scipubtts.auth.dto.RegisterLocalRequest;
import com.brotherhood.scipubtts.email.service.EmailService;
import com.brotherhood.scipubtts.email.entity.EmailVerificationToken;
import com.brotherhood.scipubtts.email.repository.EmailVerificationTokenRepository;
import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import com.brotherhood.scipubtts.auth.security.jwt.JwtTokenService;
import com.brotherhood.scipubtts.user.entity.Role;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final EmailService mailService;

    @Value("${app.backend-base-url:http://localhost:8080}")
    private String backendBaseUrl;

    public AuthServiceImpl(UserRepository userRepository,
                       EmailVerificationTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenService jwtTokenService,
                       EmailService mailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.mailService = mailService;
    }

    @Override
    @Transactional
    public String registerLocal(RegisterLocalRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .username(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.RESEARCHER)
                .emailVerified(false)
                .googleLinked(false)
                .banned(false)
                .build();

        userRepository.save(user);

        String rawToken = UUID.randomUUID().toString();

        String redirectUrl = buildRedirectUrl(request.appBaseUrl());
        System.out.println(rawToken);
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(rawToken)
                .expiresAt(OffsetDateTime.now().plusHours(24))
                .redirectUrl(redirectUrl)
                .build();

        tokenRepository.save(verificationToken);

        String verifyLink = backendBaseUrl + "/api/auth/verify-email?token=" + rawToken;

        mailService.sendVerificationEmail(user.getEmail(), verifyLink);

        return "Register success. Please check your email to verify your account.";

    }

    @Override
    @Transactional
    public String verifyEmail(String token) {

        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new RuntimeException("Verification token already used");
        }

        if (verificationToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new RuntimeException("Verification token expired");
        }

        User user = verificationToken.getUser();

        user.setEmailVerified(true);

        verificationToken.setUsedAt(OffsetDateTime.now());

        userRepository.save(user);
        tokenRepository.save(verificationToken);

        return verificationToken.getRedirectUrl();
    }

    private String buildRedirectUrl(String appBaseUrl) {
        if (appBaseUrl == null || appBaseUrl.isBlank()) {
            appBaseUrl = "http://localhost:3000";
        }

        return appBaseUrl + "/login?verified=true";
    }

    @Override
    @Transactional
    public String loginLocal(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email or password is invalid"));

        if (user.isBanned()) {
            throw new IllegalStateException("Account is banned");
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new IllegalStateException("This account was created with Google login");
        }

        if (!user.isEmailVerified()) {
            throw new IllegalStateException("Please verify email before login");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, rawPassword)
        );

        UserPrincipal principal = UserPrincipal.create(user);
        return jwtTokenService.generateAccessToken(principal);
    }
}
