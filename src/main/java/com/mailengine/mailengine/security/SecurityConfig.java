package com.mailengine.mailengine.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Configures and returns the {@link SecurityFilterChain} for the application, defining
     * security policies such as disabling CSRF, using stateless session management, authorizing
     * certain requests, and registering a JWT authentication filter.
     *
     * @param http the {@link HttpSecurity} object used to configure the security settings for the application.
     *             This includes defining various security configurations such as authentication requests, filters,
     *             and session management.
     * @return a built {@link SecurityFilterChain} containing the configured security settings.
     * @throws Exception if there is an error during the configuration process.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer :: disable)
                .sessionManagement( session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Defines a bean for the {@link PasswordEncoder} to be used in the application for encoding passwords.
     * The implementation returned is {@link BCryptPasswordEncoder}, which provides a secure mechanism
     * for hashing and verifying passwords.
     *
     * @return an instance of {@link BCryptPasswordEncoder} to handle password encoding and decoding.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures and provides an instance of {@link AuthenticationManager}, which is used for managing
     * authentication in the application. The {@link AuthenticationManager} is retrieved from the
     * provided {@link AuthenticationConfiguration}.
     *
     * @param config the {@link AuthenticationConfiguration} instance that contains the authentication
     *               manager configuration for the application.
     * @return an instance of {@link AuthenticationManager} retrieved from the provided
     *         {@link AuthenticationConfiguration}.
     * @throws Exception if there is an error while retrieving the {@link AuthenticationManager}.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
        throws Exception {
        return config.getAuthenticationManager();
        }
}
