package com.niit.quickcart;

import com.niit.quickcart.exception.BadRequestException;
import com.niit.quickcart.model.PasswordResetToken;
import com.niit.quickcart.model.User;
import com.niit.quickcart.repository.PasswordResetTokenRepository;
import com.niit.quickcart.repository.UserRepository;
import com.niit.quickcart.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private JavaMailSender mailSender;

    @Test
    void requestSendsOneTimeLinkAndStoresOnlyTokenHash() {
        User user = user();
        when(userRepository.findByEmailIgnoreCase("customer@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service().requestPasswordReset(" customer@example.com ");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(tokenRepository).deleteAllForUser(user.getId());
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(mailSender).send(mailCaptor.capture());

        String mailText = mailCaptor.getValue().getText();
        String resetLink = mailText.lines()
                .filter(line -> line.contains("reset-password.html?token="))
                .findFirst()
                .orElseThrow();
        String rawToken = URI.create(resetLink).getQuery().substring("token=".length());

        assertEquals("customer@example.com", mailCaptor.getValue().getTo()[0]);
        assertTrue(tokenCaptor.getValue().getTokenHash().matches("[0-9a-f]{64}"));
        assertNotEquals(rawToken, tokenCaptor.getValue().getTokenHash());
        assertTrue(tokenCaptor.getValue().getExpiresAt().isAfter(LocalDateTime.now()));
        assertTrue(tokenCaptor.getValue().getExpiresAt().isBefore(LocalDateTime.now().plusMinutes(31)));
    }

    @Test
    void unknownEmailDoesNotSendEmailOrCreateToken() {
        when(userRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        service().requestPasswordReset("unknown@example.com");

        verifyNoInteractions(tokenRepository, mailSender);
    }

    @Test
    void resetChangesPasswordAndInvalidatesResetTokens() {
        User user = user();
        PasswordResetToken resetToken = new PasswordResetToken(
                user, "stored-hash", LocalDateTime.now().plusMinutes(10));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(resetToken));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service().resetPassword("single-use-token", "new-password");

        assertTrue(BCrypt.checkpw("new-password", user.getPasswordHash()));
        verify(userRepository).save(user);
        verify(tokenRepository).deleteAllForUser(user.getId());
    }

    @Test
    void expiredResetTokenIsRejected() {
        PasswordResetToken resetToken = new PasswordResetToken(
                user(), "stored-hash", LocalDateTime.now().minusSeconds(1));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(resetToken));

        assertThrows(BadRequestException.class, () -> service().resetPassword("expired-token", "new-password"));
    }

    private PasswordResetService service() {
        return new PasswordResetService(
                userRepository, tokenRepository, mailSender,
                "no-reply@example.com", "http://localhost:8080/");
    }

    private static User user() {
        User user = new User("QuickCart Customer", "customer@example.com",
                BCrypt.hashpw("old-password", BCrypt.gensalt(10)));
        user.setId(17L);
        return user;
    }
}
