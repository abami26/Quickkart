package com.niit.quickcart.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niit.quickcart.dto.Dtos.AdminReviewRequest;
import com.niit.quickcart.dto.Dtos.LoginRequest;
import com.niit.quickcart.exception.BadRequestException;
import com.niit.quickcart.exception.UnauthorizedException;
import com.niit.quickcart.model.MerchantAccount;
import com.niit.quickcart.repository.MerchantAccountRepository;
import com.niit.quickcart.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Set<String> DECISIONS = Set.of("approved", "rejected", "changes_requested");

    private final MerchantAccountRepository merchantRepository;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final String adminEmail;
    private final String adminPassword;

    public AdminController(
            MerchantAccountRepository merchantRepository,
            JwtUtil jwtUtil,
            ObjectMapper objectMapper,
            @Value("${quickcart.admin.email:admin@quickcart.local}") String adminEmail,
            @Value("${quickcart.admin.password:QuickCartAdmin2026!}") String adminPassword) {
        this.merchantRepository = merchantRepository;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.adminEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        this.adminPassword = adminPassword;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        if (request.email == null || request.password == null
                || !adminEmail.equals(request.email.trim().toLowerCase(Locale.ROOT))
                || !constantTimeEquals(adminPassword, request.password)) {
            throw new UnauthorizedException("Incorrect admin email or password.");
        }
        return Map.of(
                "token", jwtUtil.generateAdminToken(adminEmail),
                "admin", Map.of("name", "QuickCart Admin", "email", adminEmail, "accountType", "admin"));
    }

    @GetMapping("/merchant-applications")
    public Map<String, Object> listApplications(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        jwtUtil.requireAdminEmail(authHeader);
        List<Map<String, Object>> applications = merchantRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
        return Map.of("applications", applications);
    }

    @PutMapping("/merchant-applications/{id}/review")
    public Map<String, Object> reviewApplication(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody AdminReviewRequest request) {
        String reviewer = jwtUtil.requireAdminEmail(authHeader);
        String decision = request.decision == null ? "" : request.decision.trim().toLowerCase(Locale.ROOT);
        if (!DECISIONS.contains(decision)) {
            throw new BadRequestException("Choose approve, reject, or request changes.");
        }

        String note = request.note == null ? "" : request.note.trim();
        if (!"approved".equals(decision) && note.isBlank()) {
            throw new BadRequestException("Add a note when rejecting or requesting changes.");
        }
        if (note.length() > 2000) {
            throw new BadRequestException("Review notes must be 2000 characters or fewer.");
        }

        MerchantAccount merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Merchant application not found."));
        if (!"pending_review".equals(merchant.getKycStatus())
                && !"changes_requested".equals(merchant.getKycStatus())) {
            throw new BadRequestException("Only pending applications can be reviewed.");
        }

        merchant.setKycStatus(decision);
        merchant.setReviewNote(note.isBlank() ? null : note);
        merchant.setReviewedBy(reviewer);
        merchant.setReviewedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());
        MerchantAccount saved = merchantRepository.save(merchant);
        return Map.of("application", toResponse(saved));
    }

    private boolean constantTimeEquals(String expected, String supplied) {
        return java.security.MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                supplied.getBytes(StandardCharsets.UTF_8));
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
}
