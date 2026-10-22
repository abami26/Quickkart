package com.niit.quickcart.dto;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Dtos {

    public static class SignupRequest {
        public String name;
        public String email;
        public String password;
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    public static class MerchantSignupRequest {
        public String ownerName;
        public String email;
        public String password;
        public String phone;
        public String storeName;
        public String category;
        public String description;
        public String address;
        public String city;
        public String state;
        public java.util.List<String> serviceAreas;
        public boolean kycConsentAccepted;
    }

    public static class MerchantProfileRequest {
        public String ownerName;
        public String phone;
        public String storeName;
        public String category;
        public String description;
        public String address;
        public String city;
        public String state;
        public java.util.List<String> serviceAreas;
    }

    public static class AdminReviewRequest {
        public String decision;
        public String note;
    }

    public static class OrderRequest {
        // e.g. { "1": 2, "3": 1 }  -> product id (as string) -> quantity
        public Map<String, Integer> items;
        public String paymentMethod;
        public String address;
        public String street;
        public String city;
        public String state;

        public String composeAddress() {
            if (address != null && !address.isBlank()) {
                return address.trim();
            }

            String safeStreet = Optional.ofNullable(street).orElse("").trim();
            String safeCity = Optional.ofNullable(city).orElse("Lagos").trim();
            String safeState = Optional.ofNullable(state).orElse("Lagos").trim();

            if (safeCity.isBlank()) {
                safeCity = "Lagos";
            }
            if (safeState.isBlank()) {
                safeState = "Lagos";
            }

            return Stream.of(safeStreet, safeCity, safeState)
                    .filter(part -> !part.isBlank())
                    .collect(Collectors.joining(", "));
        }
    }

    public static class UserSummary {
        public Long id;
        public String name;
        public String email;

        public UserSummary(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
    }

    public static class AuthResponse {
        public String token;
        public UserSummary user;

        public AuthResponse(String token, UserSummary user) {
            this.token = token;
            this.user = user;
        }
    }
}
