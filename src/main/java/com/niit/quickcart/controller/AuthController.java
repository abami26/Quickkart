package com.niit.quickcart.controller;

import com.niit.quickcart.dto.Dtos.*;
import com.niit.quickcart.exception.BadRequestException;
import com.niit.quickcart.exception.UnauthorizedException;
import com.niit.quickcart.model.User;
import com.niit.quickcart.repository.UserRepository;
import com.niit.quickcart.security.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody SignupRequest req) {
        if (req.name == null || req.name.isBlank()
                || req.email == null || req.email.isBlank()
                || req.password == null || req.password.isBlank()) {
            throw new BadRequestException("Name, email and password are all required.");
        }
        if (req.password.length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters.");
        }
        if (userRepository.findByEmailIgnoreCase(req.email).isPresent()) {
            throw new BadRequestException("An account with that email already exists.");
        }

        String hash = BCrypt.hashpw(req.password, BCrypt.gensalt(10));
        User user = new User(req.name, req.email, hash);
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, new UserSummary(user.getId(), user.getName(), user.getEmail()));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        if (req.email == null || req.email.isBlank() || req.password == null || req.password.isBlank()) {
            throw new BadRequestException("Email and password are required.");
        }

        User user = userRepository.findByEmailIgnoreCase(req.email)
                .orElseThrow(() -> new UnauthorizedException("Incorrect email or password."));

        if (!BCrypt.checkpw(req.password, user.getPasswordHash())) {
            throw new UnauthorizedException("Incorrect email or password.");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, new UserSummary(user.getId(), user.getName(), user.getEmail()));
    }

    @GetMapping("/me")
    public UserSummary me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = jwtUtil.requireUserId(authHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found."));
        return new UserSummary(user.getId(), user.getName(), user.getEmail());
    }
}
