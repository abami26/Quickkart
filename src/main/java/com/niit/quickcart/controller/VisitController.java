package com.niit.quickcart.controller;

import com.niit.quickcart.model.SiteVisitor;
import com.niit.quickcart.repository.SiteVisitorRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
public class VisitController {

    private final SiteVisitorRepository visitorRepository;

    public VisitController(SiteVisitorRepository visitorRepository) {
        this.visitorRepository = visitorRepository;
    }

    @PostMapping("/api/visits")
    public Map<String, Object> countVisitor(
            @CookieValue(name = "qc_analytics_consent", required = false) String consent,
            @CookieValue(name = "qc_visitor_id", required = false) String visitorId,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (!"accepted".equals(consent)) {
                if ("declined".equals(consent)) {
                    if (visitorId != null && isUuid(visitorId)) {
                        visitorRepository.deleteById(visitorId);
                    }
                    ResponseCookie expiredVisitorCookie = ResponseCookie.from("qc_visitor_id", "")
                            .httpOnly(true)
                            .secure(request.isSecure())
                            .path("/")
                            .sameSite("Lax")
                            .maxAge(Duration.ZERO)
                            .build();
                    response.addHeader("Set-Cookie", expiredVisitorCookie.toString());
                }
            return Map.of("tracked", false, "uniqueVisitors", visitorRepository.count());
        }

        if (visitorId == null || !isUuid(visitorId)) {
            visitorId = UUID.randomUUID().toString();
            ResponseCookie visitorCookie = ResponseCookie.from("qc_visitor_id", visitorId)
                    .httpOnly(true)
                    .secure(request.isSecure())
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(Duration.ofDays(365))
                    .build();
            response.addHeader("Set-Cookie", visitorCookie.toString());
        }

        if (!visitorRepository.existsById(visitorId)) {
            visitorRepository.save(new SiteVisitor(visitorId));
        }

        return Map.of("tracked", true, "uniqueVisitors", visitorRepository.count());
    }

    private boolean isUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}