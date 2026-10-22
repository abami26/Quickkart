package com.niit.quickcart.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niit.quickcart.dto.Dtos.LoginRequest;
import com.niit.quickcart.dto.Dtos.MerchantProfileRequest;
import com.niit.quickcart.dto.Dtos.MerchantSignupRequest;
import com.niit.quickcart.exception.BadRequestException;
import com.niit.quickcart.exception.UnauthorizedException;
import com.niit.quickcart.model.MerchantAccount;
import com.niit.quickcart.repository.MerchantAccountRepository;
import com.niit.quickcart.security.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    private static final Set<String> LAGOS_AREAS = Set.of(
            "agege", "alimosho", "ajah", "badagry", "epe", "ikeja", "kosofe",
            "lekki", "ojo", "somolu", "surulere", "victoria island", "yaba");

    private final MerchantAccountRepository merchantRepository;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public MerchantController(
            MerchantAccountRepository merchantRepository,
            JwtUtil jwtUtil,
            ObjectMapper objectMapper) {
        this.merchantRepository = merchantRepository;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/signup")
    public Map<String, Object> signup(@RequestBody MerchantSignupRequest request) {
        validateProfile(request.ownerName, request.phone, request.storeName, request.category,
                request.description, request.address, request.city, request.state, request.serviceAreas);
        if (!request.kycConsentAccepted) {
            throw new BadRequestException("Confirm the business information declaration to submit your application.");
        }
        if (request.email == null || !request.email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new BadRequestException("Enter a valid email address.");
        }
        if (request.password == null || request.password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters.");
        }
        String email = request.email.trim().toLowerCase(Locale.ROOT);
        if (merchantRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new BadRequestException("A merchant account with that email already exists.");
        }

        MerchantAccount merchant = new MerchantAccount();
        merchant.setEmail(email);
        merchant.setPasswordHash(BCrypt.hashpw(request.password, BCrypt.gensalt(10)));
        updateProfile(merchant, request.ownerName, request.phone, request.storeName, request.category,
                request.description, request.address, request.city, request.state, request.serviceAreas);
        merchant.setKycStatus("pending_review");
        merchant.setCreatedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());
        merchant.setKycConsentAt(LocalDateTime.now());
        merchant = merchantRepository.save(merchant);

        return authResponse(merchant);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        if (request.email == null || request.email.isBlank()
                || request.password == null || request.password.isBlank()) {
            throw new BadRequestException("Email and password are required.");
        }

        MerchantAccount merchant = merchantRepository.findByEmailIgnoreCase(request.email.trim())
                .orElseThrow(() -> new UnauthorizedException("Incorrect email or password."));
        if (!BCrypt.checkpw(request.password, merchant.getPasswordHash())) {
            throw new UnauthorizedException("Incorrect email or password.");
        }
        return authResponse(merchant);
    }

    @GetMapping("/me")
    public Map<String, Object> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return toResponse(requireMerchant(authHeader));
    }

    @PutMapping("/me")
    public Map<String, Object> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody MerchantProfileRequest request) {
        validateProfile(request.ownerName, request.phone, request.storeName, request.category,
                request.description, request.address, request.city, request.state, request.serviceAreas);

        MerchantAccount merchant = requireMerchant(authHeader);
        updateProfile(merchant, request.ownerName, request.phone, request.storeName, request.category,
                request.description, request.address, request.city, request.state, request.serviceAreas);
        merchant.setKycStatus("pending_review");
        merchant.setReviewNote(null);
        merchant.setReviewedBy(null);
        merchant.setReviewedAt(null);
        merchant.setUpdatedAt(LocalDateTime.now());
        return toResponse(merchantRepository.save(merchant));
    }

    private MerchantAccount requireMerchant(String authHeader) {
        Long merchantId = jwtUtil.requireMerchantId(authHeader);
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new UnauthorizedException("Merchant account not found."));
    }

    private Map<String, Object> authResponse(MerchantAccount merchant) {
        return Map.of(
                "token", jwtUtil.generateMerchantToken(merchant),
                "merchant", toResponse(merchant));
    }

    private Map<String, Object> toResponse(MerchantAccount merchant) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", merchant.getId());
        response.put("ownerName", merchant.getOwnerName());
        response.put("email", merchant.getEmail());
        response.put("phone", merchant.getPhone());
        response.put("storeName", merchant.getStoreName());
        response.put("category", merchant.getCategory());
        response.put("description", merchant.getDescription());
        response.put("address", merchant.getAddress());
        response.put("city", merchant.getCity());
        response.put("state", merchant.getState());
        response.put("serviceAreas", readServiceAreas(merchant.getServiceAreasJson()));
        response.put("kycStatus", merchant.getKycStatus());
        response.put("reviewNote", merchant.getReviewNote());
        response.put("reviewedBy", merchant.getReviewedBy());
        response.put("reviewedAt", merchant.getReviewedAt() == null ? null : merchant.getReviewedAt().toString());
        response.put("createdAt", merchant.getCreatedAt().toString());
        response.put("updatedAt", merchant.getUpdatedAt().toString());
        return response;
    }

    private List<String> readServiceAreas(String areasJson) {
        try {
            return objectMapper.readValue(areasJson, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Could not read merchant service areas.", e);
        }
    }

    private void updateProfile(
            MerchantAccount merchant, String ownerName, String phone, String storeName,
            String category, String description, String address, String city, String state,
            List<String> serviceAreas) {
        merchant.setOwnerName(ownerName.trim());
        merchant.setPhone(phone.trim());
        merchant.setStoreName(storeName.trim());
        merchant.setCategory(category.trim());
        merchant.setDescription(description == null ? "" : description.trim());
        merchant.setAddress(address.trim());
        merchant.setCity(city.trim());
        merchant.setState("Lagos");
        try {
            merchant.setServiceAreasJson(objectMapper.writeValueAsString(normalizeAreas(serviceAreas)));
        } catch (Exception e) {
            throw new IllegalStateException("Could not save merchant service areas.", e);
        }
    }

    private void validateProfile(
            String ownerName, String phone, String storeName, String category, String description,
            String address, String city, String state, List<String> serviceAreas) {
        requireText(ownerName, "Owner name", 120);
        requireText(phone, "Phone number", 30);
        requireText(storeName, "Store name", 120);
        requireText(category, "Store category", 80);
        requireText(address, "Store address", 240);
        requireText(city, "Store city", 80);
        if (description != null && description.length() > 1000) {
            throw new BadRequestException("Store description must be 1000 characters or fewer.");
        }
        if (state == null || !"lagos".equalsIgnoreCase(state.trim())) {
            throw new BadRequestException("Merchant onboarding is currently available in Lagos State only.");
        }
        String normalizedCity = city.trim().toLowerCase(Locale.ROOT);
        if (!LAGOS_AREAS.contains(normalizedCity)) {
            throw new BadRequestException("Choose the store city from the Lagos area list.");
        }
        if (serviceAreas == null || serviceAreas.isEmpty()) {
            throw new BadRequestException("Choose at least one Lagos service area.");
        }
        if (!normalizeAreas(serviceAreas).stream().anyMatch(area ->
                area.toLowerCase(Locale.ROOT).equals(normalizedCity))) {
            throw new BadRequestException("Include the store's city in its service areas.");
        }
    }

    private void requireText(String value, String label, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(label + " is required.");
        }
        if (value.trim().length() > maxLength) {
            throw new BadRequestException(label + " must be " + maxLength + " characters or fewer.");
        }
    }

    private List<String> normalizeAreas(List<String> serviceAreas) {
        LinkedHashMap<String, String> normalized = new LinkedHashMap<>();
        for (String area : serviceAreas) {
            if (area == null || area.isBlank()) continue;
            String key = area.trim().toLowerCase(Locale.ROOT);
            if (!LAGOS_AREAS.contains(key)) {
                throw new BadRequestException("Select service areas from the Lagos area list.");
            }
            normalized.putIfAbsent(key, toDisplayName(key));
        }
        if (normalized.isEmpty()) {
            throw new BadRequestException("Choose at least one Lagos service area.");
        }
        return List.copyOf(normalized.values());
    }

    private String toDisplayName(String area) {
        if ("victoria island".equals(area)) return "Victoria Island";
        return Arrays.stream(area.split(" "))
                .map(part -> part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1))
                .reduce((left, right) -> left + " " + right)
                .orElse(area);
    }
}
