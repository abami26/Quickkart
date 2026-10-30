package com.niit.quickcart.repository;

import com.niit.quickcart.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from PasswordResetToken token where token.user.id = :userId")
    void deleteAllForUser(@Param("userId") Long userId);
}
