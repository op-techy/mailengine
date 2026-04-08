package com.mailengine.mailengine.service;

import com.mailengine.mailengine.dto.request.*;
import com.mailengine.mailengine.dto.response.AuthResponse;
import com.mailengine.mailengine.entity.Account;
import com.mailengine.mailengine.entity.EmailVerificationToken;
import com.mailengine.mailengine.entity.PasswordResetToken;
import com.mailengine.mailengine.entity.User;
import com.mailengine.mailengine.entity.enums.Role;
import com.mailengine.mailengine.exception.BadRequestException;
import com.mailengine.mailengine.exception.ConflictException;
import com.mailengine.mailengine.exception.UnauthorizedException;
import com.mailengine.mailengine.repository.AccountRepository;
import com.mailengine.mailengine.repository.EmailVerificationTokenRepository;
import com.mailengine.mailengine.repository.PasswordResetTokenRepository;
import com.mailengine.mailengine.repository.UserRepository;
import com.mailengine.mailengine.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ── Register ─────────────────────────────────────────────────────────────

    /**
     * Registers a new user and an associated account. The method performs the following:
     * - Checks if an email address is already in use, throwing a conflict exception if so.
     * - Creates and saves a new account using the company name from the request.
     * - Creates a new user with the provided details and assigns the user an admin role.
     * - Generates an email verification token valid for 24 hours.
     * - Sends a verification email containing the token.
     *
     * @param request the registration request containing user and account details such as email,
     *                password, name, and company name
     * @throws ConflictException if an account with the provided email already exists
     */
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already in use");
        }

        Account account = new Account();
        account.setCompanyName(request.getCompanyName());
        accountRepository.save(account);

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.admin);
        user.setAccount(account);
        user.setEmailVerified(false);
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        verificationTokenRepository.save(
                new EmailVerificationToken(token, user, Instant.now().plus(24, ChronoUnit.HOURS)));

        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    // ── Verify email ──────────────────────────────────────────────────────────

    /**
     * Verifies an email address using the provided verification token. This method completes
     * the email verification process by checking the validity of the token, ensuring it has
     * not been used or expired, and marking the associated user as verified. It also updates
     * the token's status to indicate it has been used.
     *
     * @param token the email verification token to be validated and processed
     * @throws BadRequestException if the token is invalid, expired, or already used
     */
    public void verifyEmail(String token) {
        EmailVerificationToken evt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification link"));

        if (evt.isUsed() || evt.isExpired()) {
            throw new BadRequestException("Verification link has expired or already been used");
        }

        User user = evt.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        evt.setUsed(true);
        verificationTokenRepository.save(evt);
    }

    // ── Login ─────────────────────────────────────────────────────────────────
    /**
     * Authenticates a user based on the provided login request and generates a JWT token upon successful authentication.
     *
     * @param request The login request containing the user's email and password.
     * @return An authentication response containing the generated token, user details, and additional metadata.
     * @throws BadRequestException If the email or password is invalid.
     */
    public AuthResponse login(LoginRequest request) {
        // Always use the same error message to avoid user enumeration
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        long expiration = jwtService.getExpirationTime();

        return new AuthResponse(
                token,
                new AuthResponse.UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole()),
                expiration
        );
    }

    // ── Change password (authenticated) ──────────────────────────────────────
    /**
     * Updates the current user's password to the new password provided in the request.
     * Validates the current password before performing the update.
     *
     * @param request the request object containing the current password and the new password
     * @throws BadRequestException if the current password does not match the user's existing password
     */
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePwd(false);
        userRepository.save(user);
    }

    // ── Forgot password ───────────────────────────────────────────────────────
    /**
     * Handles the password reset process by generating and sending a password reset token
     * to the specified email if the user exists. The system ensures security by not revealing
     * whether the email corresponds to an existing user.
     *
     * @param request The request containing the email address for the password reset process.
     */
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always respond 200 — never reveal whether the email exists
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            // Invalidate any existing tokens for this user
            passwordResetTokenRepository.deleteByUserId(user.getId());

            String token = UUID.randomUUID().toString();
            passwordResetTokenRepository.save(
                    new PasswordResetToken(token, user, Instant.now().plus(1, ChronoUnit.HOURS)));

            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });
    }

    // ── Reset password ────────────────────────────────────────────────────────
    /**
     * Resets the password for a user based on a password reset request.
     * Validates the reset token, ensures it is not expired or already used,
     * updates the user's password, and marks the token as used.
     *
     * @param request an instance of {@code ResetPasswordRequest} containing the reset token
     *                and the new password to be set.
     * @throws BadRequestException if the reset token is invalid, expired, or already used.
     */
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset link"));

        if (prt.isUsed() || prt.isExpired()) {
            throw new BadRequestException("Reset link has expired or already been used");
        }

        User user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePwd(false);
        userRepository.save(user);

        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    /**
     * Retrieves the current authenticated user from the security context.
     *
     * @return the currently authenticated {@code User} object, or {@code null} if no user is authenticated.
     */
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
