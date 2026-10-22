package com.niit.quickcart.security;

import com.niit.quickcart.exception.UnauthorizedException;
import com.niit.quickcart.model.MerchantAccount;
import com.niit.quickcart.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Locale;
import java.util.Date;

/**
 * Creates and validates the JWT ("login token") that proves who a request is from.
 * In a real production app this secret would come from an environment variable,
 * not be hardcoded — but for a class project this is fine.
 */
@Component
public class JwtUtil {

    private static final String SECRET = "QuickCartSuperSecretJWTSigningKeyForClassProject2026!!";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRY_MILLIS = 1000L * 60 * 60 * 24 * 7; // 7 days

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRY_MILLIS);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("accountType", "customer")
                .claim("name", user.getName())
                .claim("email", user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateMerchantToken(MerchantAccount merchant) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRY_MILLIS);

        return Jwts.builder()
                .setSubject(String.valueOf(merchant.getId()))
                .claim("accountType", "merchant")
                .claim("name", merchant.getOwnerName())
                .claim("email", merchant.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAdminToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRY_MILLIS);
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        return Jwts.builder()
                .setSubject(normalizedEmail)
                .claim("accountType", "admin")
                .claim("name", "QuickCart Admin")
                .claim("email", normalizedEmail)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Pulls the user id out of an "Authorization: Bearer <token>" header.
     * Throws UnauthorizedException (-> HTTP 401) if missing or invalid.
     */
    public Long requireUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Please log in first.");
        }
        String token = authHeader.substring(7);

        try {
            Claims claims = parseClaims(token);
            String accountType = claims.get("accountType", String.class);
            if (accountType != null && !"customer".equals(accountType)) {
                throw new UnauthorizedException("A customer account is required.");
            }
            return Long.parseLong(claims.getSubject());
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Your session expired. Please log in again.");
        }
    }

    public Long requireMerchantId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Please log in to your merchant account first.");
        }

        try {
            Claims claims = parseClaims(authHeader.substring(7));
            if (!"merchant".equals(claims.get("accountType", String.class))) {
                throw new UnauthorizedException("A merchant account is required.");
            }
            return Long.parseLong(claims.getSubject());
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Your merchant session expired. Please log in again.");
        }
    }

    public String requireAdminEmail(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Please log in as an admin first.");
        }

        try {
            Claims claims = parseClaims(authHeader.substring(7));
            if (!"admin".equals(claims.get("accountType", String.class))) {
                throw new UnauthorizedException("Admin access is required.");
            }
            return claims.getSubject();
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Your admin session expired. Please log in again.");
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
