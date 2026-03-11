package com.niveshtrack.portfolio.controller;

import com.niveshtrack.portfolio.dto.request.UpdateProfileRequest;
import com.niveshtrack.portfolio.dto.response.UserProfileDTO;
import com.niveshtrack.portfolio.security.UserDetailsServiceImpl;
import com.niveshtrack.portfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User profile view and update endpoints.
 */
@RestController
@RequestMapping("/api/user")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "User Profile", description = "User profile management APIs")
public class UserController extends BaseController {

    private final UserService userService;

    public UserController(UserDetailsServiceImpl userDetailsService,
                           UserService userService) {
        super(userDetailsService);
        this.userService = userService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get user profile",
               description = "Returns the authenticated user's profile information.")
    public ResponseEntity<UserProfileDTO> getProfile() {
        return ResponseEntity.ok(userService.getUserProfile(getCurrentUserId()));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile",
               description = "Partially updates name, email, password, currency, or dark mode preference.")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(getCurrentUserId(), request));
    }
}
