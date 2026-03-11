package com.niveshtrack.portfolio.controller;

import com.niveshtrack.portfolio.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Base class providing a helper method to extract the authenticated user's ID
 * from the Spring Security context without an extra DB call per controller.
 */
@RequiredArgsConstructor
public abstract class BaseController {

    protected final UserDetailsServiceImpl userDetailsService;

    /**
     * Extracts the current user's database ID from the security context.
     *
     * @return the Long user ID
     */
    protected Long getCurrentUserId() {
        UserDetails principal = (UserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return userDetailsService.loadUserEntityByEmail(principal.getUsername()).getId();
    }
}
