package com.tarique.bankstatement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarique.bankstatement.repository.UserRepository;
import com.tarique.bankstatement.security.jwt.JwtAuthEntryPoint;
import com.tarique.bankstatement.security.jwt.JwtAuthTokenFilter;
import com.tarique.bankstatement.security.jwt.JwtProvider;
import com.tarique.bankstatement.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig — Spring Security 6 configuration.
 *
 * <p>Stateless JWT-based auth. CSRF disabled (API only, no browser sessions).
 * Public endpoints: /api/v1/auth/**, Swagger UI, Actuator health.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthEntryPoint      jwtAuthEntryPoint;
    private final JwtProvider            jwtProvider;
    private final UserRepository         userRepository;
    private final ObjectMapper           objectMapper;

    @Bean
    public JwtAuthTokenFilter jwtAuthTokenFilter() {
        return new JwtAuthTokenFilter(jwtProvider, userDetailsService, userRepository, objectMapper);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — stateless REST API
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless session — JWT per request
            .sessionManagement(sm ->
                    sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Entry point for 401 responses
            .exceptionHandling(ex ->
                    ex.authenticationEntryPoint(jwtAuthEntryPoint))

            // Route authorization
            .authorizeHttpRequests(auth -> auth
                    // Public endpoints
                    .requestMatchers(
                            "/api/v1/auth/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**",
                            "/actuator/health",
                            "/actuator/info"
                    ).permitAll()
                    // Everything else requires authentication
                    .anyRequest().authenticated()
            )

            // Register JWT filter before Spring's username/password filter
            .addFilterBefore(jwtAuthTokenFilter(), UsernamePasswordAuthenticationFilter.class)

            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
