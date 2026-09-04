package com.attendance.face.config;

import com.attendance.face.config.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final List<String> allowedOrigins;

    @Autowired
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthFilter,
            @Value("${cors.allowed.origins}") String allowedOrigins) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Enable CORS using our bean
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Disable CSRF (JWT stateless APIs)
                .csrf(AbstractHttpConfigurer::disable)

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // IMPORTANT: allow preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public endpoints
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/students/register-with-photo",
                                "/api/health",
                                "/health"
                        ).permitAll()

                        // STUDENT endpoints
                        .requestMatchers(
                                "/api/attendance/sessions/*/mark-by-face",
                                "/api/attendance/active-sessions",
                                "/api/attendance/my-attendance"
                        ).hasRole("STUDENT")

                        // LECTURER endpoints
                        .requestMatchers("/api/lecturer/**")
                        .hasRole("LECTURER")

                        // ADMIN endpoints
                        .requestMatchers(
                                "/api/admin/**",
                                "/api/students/**",
                                "/upload/**",
                                "/api/test/**",
                                "/api/attendance/today",
                                "/api/attendance/date/**",
                                "/api/attendance/student/**",
                                "/api/attendance/session/**"
                        )
                        .hasRole("ADMIN")

                        // Deny new or forgotten endpoints until explicitly classified above.
                        .anyRequest().denyAll()
                )

                // Stateless session (JWT system)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                //  JWT filter BEFORE username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (JWT headers)
        config.setAllowCredentials(true);

        config.setAllowedOrigins(allowedOrigins);

        // Allowed methods
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Allow all headers (Authorization is important for JWT)
        config.setAllowedHeaders(List.of("*"));

        // Optional caching for preflight
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
