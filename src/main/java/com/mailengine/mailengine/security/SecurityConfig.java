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
     * Configures the security filter chain for the application using the provided {@link HttpSecurity}.
     * This method customizes the security settings, such as disabling CSRF, form login, and HTTP Basic authentication,
     * setting the session management policy to stateless, and defining security rules for HTTP request authorization.
     * Additionally, it adds a {@link JwtAuthFilter} before the {@link UsernamePasswordAuthenticationFilter}.
     *
     * @param http the {@link HttpSecurity} instance used to configure security for the application.
     *             This includes defining the security filters, session management, and request authorization rules.
     * @return the built {@link SecurityFilterChain} that defines the security configuration for the application.
     * @throws Exception if an error occurs while configuring the {@link HttpSecurity}.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer :: disable)
                .formLogin(AbstractHttpConfigurer :: disable)
                .httpBasic(AbstractHttpConfigurer :: disable)
                .sessionManagement( session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**"
                                , "/v3/api-docs/**"
                                , "/swagger-ui/**"
                                , "/swagger-ui.html").permitAll()
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
