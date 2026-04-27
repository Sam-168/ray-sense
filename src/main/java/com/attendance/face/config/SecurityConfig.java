package com.attendance.face.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

                .csrf(AbstractHttpConfigurer::disable)


                .authorizeHttpRequests(auth -> auth


                        .requestMatchers(
                                "/api/auth/**",        // Login and register
                                "/api/health",         // Health check
                                "/api/face-recognition/health"  // Python service health
                        ).permitAll()


                        .requestMatchers(
                                "/api/attendance/mark-by-face",
                                "/api/attendance/student/**"
                        ).hasRole("STUDENT")

                        // ===== LECTURER ONLY ROUTES =====
                        .requestMatchers(
                                "/api/lecturer/**"
                        ).hasRole("LECTURER")


                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")


                        .requestMatchers(
                                "/api/students/**",
                                "/api/attendance/**"
                        ).hasAnyRole("STUDENT", "LECTURER", "ADMIN")


                        .anyRequest().authenticated()
                )


                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
