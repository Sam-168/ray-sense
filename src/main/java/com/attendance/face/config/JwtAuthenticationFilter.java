package com.attendance.face.config;
import com.attendance.face.repository.UserRepository;
import com.attendance.face.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter  extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /**
     * This filter runs on EVERY request before it reaches controllers
     * It checks if the request has a valid JWT token
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Step 1: Get the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Step 2: If no Authorization header or doesn't start with "Bearer ", skip
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token - let request proceed (security config decides if route is public)
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract token (remove "Bearer " prefix)
        final String token = authHeader.substring(7);

        try {
            // Step 4: Extract email from token
            final String email = jwtService.extractEmail(token);

            // Step 5: If email extracted and no authentication set yet
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Step 6: Load user from database
                var userOptional = userRepository.findByEmail(email);

                if (userOptional.isPresent()) {
                    var user = userOptional.get();

                    // Step 7: Validate token
                    if (jwtService.isTokenValid(token, email) && user.getIsActive()) {

                        // Step 8: Create Spring Security authentication object
                        // This tells Spring Security "this user is authenticated"
                        var authToken = new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                // Grant authority based on role (ROLE_STUDENT, ROLE_LECTURER, ROLE_ADMIN)
                                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                        );

                        // Step 9: Add request details
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Step 10: Set authentication in context
                        // This is what makes @PreAuthorize work
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (Exception e) {
            // Invalid token - just continue without authentication
            // The security config will reject protected routes
            System.err.println("JWT validation failed: " + e.getMessage());
        }

        // Step 11: Continue to next filter/controller
        filterChain.doFilter(request, response);
    }
}
