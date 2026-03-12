package com.niveshtrack.portfolio.security;

import com.niveshtrack.portfolio.entity.User;
import com.niveshtrack.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Spring Security's {@link UserDetailsService} backed by the MySQL {@code users} table.
 *
 * <p>The "username" in Spring Security's context is the user's email address.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by email (used by Spring Security during authentication).
     *
     * @param email the email address to look up
     * @return a UserDetails instance wrapping the database user
     * @throws UsernameNotFoundException if no user with that email exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(Collections.emptyList())
                .build();
    }

    /**
     * Loads the full {@link User} entity by email.
     * Used by service layer to get the user ID from a security context principal.
     */
    @Transactional(readOnly = true)
    public User loadUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));
    }
}
