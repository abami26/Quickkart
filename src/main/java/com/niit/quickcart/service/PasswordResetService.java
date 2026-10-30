package com.niit.quickcart.service;

import com.niit.quickcart.exception.BadRequestException;
import com.niit.quickcart.model.PasswordResetToken;
import com.niit.quickcart.model.User;
import com.niit.quickcart.repository.PasswordResetTokenRepository;
import com.niit.quickcart.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {

    private static final int TOKEN_BYTES = 32;
    private static final int TOKEN_LIFETIME_MINUTES = 30;
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String senderAddress;
    private final String frontendBaseUrl;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            JavaMailSender mailSender,
            @Value("${quickcart.mail.from:no-reply@quickcart.local}") String senderAddress,
            @Value("${quickcart.frontend.base-url:http://localhost:8080}") String frontendBaseUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
        this.senderAddress = senderAddress;
        this.frontendBaseUrl = frontendBaseUrl.replaceAll("/+$", "");
    }

    @Transactional
    public void requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required.");
        }

        User user = userRepository.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (user == null) {
            return;
        }

        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_LIFETIME_MINUTES);

        tokenRepository.deleteAllForUser(user.getId());
        tokenRepository.save(new PasswordResetToken(user, hashToken(token), expiresAt));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderAddress);
        message.setTo(user.getEmail());
        message.setSubject("Reset your QuickCart password");
        message.setText("We received a request to reset your QuickCart password. "
                + "Use this link within 30 minutes to choose a new password:\n\n"
                + frontendBaseUrl + "/reset-password.html?token=" + token
                + "\n\nIf you did not request this, you can ignore this email.");
        mailSender.send(message);
    }

    @Transactional
    public void resetPassword(String token, String password) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("The password reset link is invalid or has expired.");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new BadRequestException("Password must be at least 6 characters.");
        }

        PasswordResetToken resetToken = tokenRepository.findByTokenHash(hashToken(token))
                .orElseThrow(() -> new BadRequestException("The password reset link is invalid or has expired."));
        if (!resetToken.getExpiresAt().isAfter(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new BadRequestException("The password reset link is invalid or has expired.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(10)));
        userRepository.save(user);
        tokenRepository.deleteAllForUser(user.getId());
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Could not secure the password reset token.", exception);
        }
    }
}
