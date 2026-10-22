package com.niit.quickcart.repository;

import com.niit.quickcart.model.MerchantAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface MerchantAccountRepository extends JpaRepository<MerchantAccount, Long> {
    Optional<MerchantAccount> findByEmailIgnoreCase(String email);
    List<MerchantAccount> findAllByOrderByCreatedAtDesc();
}
