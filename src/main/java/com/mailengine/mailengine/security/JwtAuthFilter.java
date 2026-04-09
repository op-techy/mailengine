package com.mailengine.mailengine.security;

import com.mailengine.mailengine.entity.User;
import com.mailengine.mailengine.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    /**
     * Processes the incoming HTTP request to handle JWT authentication. This filter checks
     * for the presence of a JWT in the "Authorization" header, validates the token, and sets
     * the corresponding authentication context for the user if the token is valid.
     * If the token is invalid or absent, the filter allows the request to proceed without
     * setting the authentication context.
     *
     * @param request the incoming {@link HttpServletRequest} instance that carries
     *                the HTTP request details, including headers and body.
     * @param response the {@link HttpServletResponse} instance used to send a response
     *                 back to the client.
     * @param filterChain the {@link FilterChain} instance that allows the request to proceed
     *                    through the rest of the filter chain.
     * @throws ServletException if an issue occurs during the filtering process.
     * @throws IOException if an IO error occurs while processing the request or response.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip if already authenticated (e.g. subsequent filters in the chain)
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try{
            if (!jwtService.isTokenValid(jwt)){
                filterChain.doFilter(request, response);
                return;
            }
            String userId = jwtService.extractUserId(jwt);
            User user = userRepository.findById(Long.parseLong(userId)).orElseThrow(() -> new RuntimeException("User not found"));

            if (user != null){
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authenticated user: {} role: {}", user.getEmail(), user.getRole());
            }
        } catch (Exception e){
            log.debug("JWT authentication failed for {}: {}", request.getRequestURI(), e.getMessage());
        }
        filterChain.doFilter(request, response);

    }
}
