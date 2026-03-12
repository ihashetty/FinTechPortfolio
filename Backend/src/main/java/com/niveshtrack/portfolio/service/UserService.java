package com.niveshtrack.portfolio.service;

import com.niveshtrack.portfolio.dto.request.UpdateProfileRequest;
import com.niveshtrack.portfolio.dto.response.UserProfileDTO;
import com.niveshtrack.portfolio.entity.User;
import com.niveshtrack.portfolio.exception.DuplicateResourceException;
import com.niveshtrack.portfolio.exception.ResourceNotFoundException;
import com.niveshtrack.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User profile management (view and update).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Returns a user's profile data.
     */
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return toDTO(user);
    }

    /**
     * Partially updates a user's profile.
     * Validates email uniqueness if a new email is provided.
     */
    @Transactional
    public UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equalsIgnoreCase(user.getEmail())
                    && userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("User", "email", request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getCurrency() != null) {
            user.setCurrency(request.getCurrency());
        }

        if (request.getDarkMode() != null) {
            user.setDarkMode(request.getDarkMode());
        }

        User updated = userRepository.save(user);
        log.info("Profile updated for userId={}", userId);
        return toDTO(updated);
    }

    // ===== Helper =====

    private UserProfileDTO toDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .currency(user.getCurrency())
                .darkMode(user.getDarkMode())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
