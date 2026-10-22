package com.niit.quickcart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niit.quickcart.controller.MerchantController;
import com.niit.quickcart.dto.Dtos.MerchantProfileRequest;
import com.niit.quickcart.dto.Dtos.MerchantSignupRequest;
import com.niit.quickcart.model.MerchantAccount;
import com.niit.quickcart.repository.MerchantAccountRepository;
import com.niit.quickcart.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MerchantControllerTest {

    @Test
    void signupCreatesPendingMerchantWithoutReturningPassword() {
        AtomicReference<MerchantAccount> savedMerchant = new AtomicReference<>();
        MerchantAccountRepository repository = repository(savedMerchant);
        JwtUtil jwtUtil = new JwtUtil();
        MerchantController controller = new MerchantController(repository, jwtUtil, new ObjectMapper());
        MerchantSignupRequest request = validSignup();

        Map<String, Object> response = controller.signup(request);
        Map<?, ?> merchant = (Map<?, ?>) response.get("merchant");
        MerchantAccount saved = savedMerchant.get();

        assertNotNull(response.get("token"));
        assertEquals("pending_review", merchant.get("kycStatus"));
        assertFalse(merchant.containsKey("passwordHash"));
        assertNotEquals(request.password, saved.getPasswordHash());
        assertTrue(BCrypt.checkpw(request.password, saved.getPasswordHash()));
        assertEquals(List.of("Ikeja", "Agege"), merchant.get("serviceAreas"));
        assertEquals(saved.getId(), jwtUtil.requireMerchantId("Bearer " + response.get("token")));
    }

    @Test
    void profileUpdatesKeepApplicationPending() {
        AtomicReference<MerchantAccount> savedMerchant = new AtomicReference<>();
        MerchantAccountRepository repository = repository(savedMerchant);
        JwtUtil jwtUtil = new JwtUtil();
        MerchantController controller = new MerchantController(repository, jwtUtil, new ObjectMapper());
        Map<String, Object> signupResponse = controller.signup(validSignup());
        String token = "Bearer " + signupResponse.get("token");

        MerchantProfileRequest update = new MerchantProfileRequest();
        update.ownerName = "Ada Owner";
        update.phone = "+234 801 000 0000";
        update.storeName = "Updated Market";
        update.category = "Groceries & essentials";
        update.description = "Updated profile";
        update.address = "5 Allen Avenue";
        update.city = "Ikeja";
        update.state = "Lagos";
        update.serviceAreas = List.of("Ikeja", "Yaba");

        Map<String, Object> result = controller.updateProfile(token, update);

        assertEquals("Updated Market", result.get("storeName"));
        assertEquals("pending_review", result.get("kycStatus"));
        assertEquals(List.of("Ikeja", "Yaba"), result.get("serviceAreas"));
    }

    private static MerchantSignupRequest validSignup() {
        MerchantSignupRequest request = new MerchantSignupRequest();
        request.ownerName = "Ada Owner";
        request.email = "merchant@example.com";
        request.password = "safe-demo-password";
        request.phone = "+234 801 000 0000";
        request.storeName = "Ada's Market";
        request.category = "Groceries & essentials";
        request.description = "Fresh goods";
        request.address = "5 Allen Avenue";
        request.city = "Ikeja";
        request.state = "Lagos";
        request.serviceAreas = List.of("Ikeja", "Agege");
        request.kycConsentAccepted = true;
        return request;
    }

    private static MerchantAccountRepository repository(AtomicReference<MerchantAccount> saved) {
        return (MerchantAccountRepository) Proxy.newProxyInstance(
                MerchantAccountRepository.class.getClassLoader(),
                new Class<?>[]{MerchantAccountRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByEmailIgnoreCase" -> Optional.empty();
                    case "findById" -> {
                        MerchantAccount account = saved.get();
                        yield account != null && account.getId().equals(args[0])
                                ? Optional.of(account) : Optional.empty();
                    }
                    case "save" -> {
                        MerchantAccount account = (MerchantAccount) args[0];
                        account.setId(7L);
                        saved.set(account);
                        yield account;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }
}
