package com.mailengine.mailengine.service;

import com.mailengine.mailengine.dto.request.LoginRequest;
import com.mailengine.mailengine.dto.request.RegisterRequest;
import com.mailengine.mailengine.dto.response.AuthResponse;
import com.mailengine.mailengine.entity.Account;
import com.mailengine.mailengine.entity.User;
import com.mailengine.mailengine.entity.enums.Role;
import com.mailengine.mailengine.repository.AccountRepository;
import com.mailengine.mailengine.repository.UserRepository;
import com.mailengine.mailengine.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user and their associated account, and returns an authentication response with a JWT token.
     * The method performs the following steps:
     * - Checks if the email provided in the request is already in use.
     * - Creates a new account and associates it with the user.
     * - Creates an admin user with the provided details.
     * - Generates a JWT token for the created user.
     *
     * @param request the registration request containing user and account details, including name, email,
     *                password, and company name
     * @return an {@code AuthResponse} object containing the JWT token, user ID, email, and user role
     * @throws RuntimeException if the email provided in the request is already in use
     */
    public AuthResponse register(RegisterRequest request){

        // Check if user already exists
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already in use");
        }

        // Create account
        Account account = new Account();
        account.setCompanyName(request.getCompanyName());
        accountRepository.save(account);

        // Create admin user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.admin);
        user.setAccount(account);
        userRepository.save(user);

        // Generate JWT
        String token = jwtService.generateToken(user);

        // Return response - no password or sensitive data
        return new AuthResponse(token,user.getId(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request){

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Wrong email or password"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            throw new RuntimeException("Wrong email or password");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(token,user.getId(), user.getEmail(), user.getRole());
    }
}
