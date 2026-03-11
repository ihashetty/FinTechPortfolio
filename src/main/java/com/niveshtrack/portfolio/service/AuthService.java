package com.niveshtrack.portfolio.service;

import com.niveshtrack.portfolio.dto.request.LoginRequest;
import com.niveshtrack.portfolio.dto.request.RegisterRequest;
import com.niveshtrack.portfolio.dto.response.AuthResponse;
import com.niveshtrack.portfolio.entity.RefreshToken;
import com.niveshtrack.portfolio.entity.User;
import com.niveshtrack.portfolio.exception.DuplicateResourceException;
import com.niveshtrack.portfolio.exception.ValidationException;
import com.niveshtrack.portfolio.repository.RefreshTokenRepository;
import com.niveshtrack.portfolio.repository.UserRepository;
import com.niveshtrack.portfolio.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handles user registration, login, and token refresh.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    // ===== Register =====

    /**
     * Registers a new user. Throws {@link DuplicateResourceException} if email is taken.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .currency("INR")
                .darkMode(false)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtTokenProvider.generateToken(userDetails);
        String refreshToken = createAndSaveRefreshToken(user, userDetails);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ===== Login =====

    /**
     * Authenticates credentials and returns JWT tokens.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Will throw BadCredentialsException if invalid
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidationException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtTokenProvider.generateToken(userDetails);

        // Revoke existing refresh tokens for this user and issue a new one
        refreshTokenRepository.deleteByUserId(user.getId());
        String refreshToken = createAndSaveRefreshToken(user, userDetails);

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ===== Refresh Token =====

    /**
     * Validates a refresh token and issues a new access token.
     */
    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new ValidationException("Invalid or expired refresh token"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new ValidationException("Refresh token has expired. Please log in again.");
        }

        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtTokenProvider.generateToken(userDetails);

        return buildAuthResponse(user, newAccessToken, refreshTokenStr);
    }

    // ===== Helpers =====

    private String createAndSaveRefreshToken(User user, UserDetails userDetails) {
        String tokenStr = jwtTokenProvider.generateRefreshToken(userDetails);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenStr)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);
        return tokenStr;
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .currency(user.getCurrency())
                .darkMode(user.getDarkMode())
                .build();
    }
}
