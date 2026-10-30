package com.niit.quickcart;

import com.niit.quickcart.model.PasswordResetToken;
import com.niit.quickcart.model.User;
import com.niit.quickcart.repository.PasswordResetTokenRepository;
import com.niit.quickcart.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class PasswordResetTokenRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Test
    void deletesOnlyTokensOwnedByRequestedUser() {
        User firstUser = userRepository.save(new User("First User", "first@example.com", "hash"));
        User secondUser = userRepository.save(new User("Second User", "second@example.com", "hash"));
        tokenRepository.save(new PasswordResetToken(firstUser, "first-token-hash", LocalDateTime.now().plusMinutes(30)));
        tokenRepository.save(new PasswordResetToken(secondUser, "second-token-hash", LocalDateTime.now().plusMinutes(30)));
        tokenRepository.flush();

        tokenRepository.deleteAllForUser(firstUser.getId());

        assertTrue(tokenRepository.findByTokenHash("first-token-hash").isEmpty());
        assertTrue(tokenRepository.findByTokenHash("second-token-hash").isPresent());
    }
}
