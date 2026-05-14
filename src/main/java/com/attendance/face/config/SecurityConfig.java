package com.attendance.face.config;

import com.attendance.face.config.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
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
                                "/api/face-recognition/health"
                        ).permitAll()

                        // STUDENT endpoints
                        .requestMatchers(
                                "/api/attendance/sessions/*/mark-by-face",
                                "/api/attendance/active-sessions",
                                "/api/attendance/my-attendance",
                                "/api/attendance/student/**"
                        ).hasRole("STUDENT")

                        // LECTURER endpoints
                        .requestMatchers(
                                "/api/lecturer/**",
                                "/api/lecturer/sections/*/sessions/start",
                                "/api/lecturer/sessions/*/end",
                                "/api/lecturer/sessions/*/live"
                        ).hasRole("LECTURER")

                        // ADMIN endpoints
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Authenticated fallback
                        .anyRequest().authenticated()
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

        // Allowed origins (DEV + PROD)
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://ray-sense.vercel.app"
        ));

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