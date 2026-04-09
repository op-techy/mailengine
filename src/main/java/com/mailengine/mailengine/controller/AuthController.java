package com.mailengine.mailengine.controller;

import com.mailengine.mailengine.dto.request.*;
import com.mailengine.mailengine.dto.response.AuthResponse;
import com.mailengine.mailengine.dto.response.MessageResponse;
import com.mailengine.mailengine.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user account with the provided registration details. The user must verify their
     * email to complete the registration process.
     *
     * @param request the registration request containing the user details such as email, password,
     *                and any additional required information
     * @return a map containing a success message indicating the account creation status
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Map.of("message", "Account created. Check your email to verify.");
    }

    /**
     * Verifies a user's email using the provided token. The token is extracted
     * from the request body and is used to confirm the user's email address.
     *
     * @param body the request body containing the token required for email verification
     * @return a map containing a success message indicating that the email verification was successful
     */
    @GetMapping("/verify-email")
    public Map<String, String> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return Map.of("message", "Email verified");
    }

    /**
     * Authenticates a user using the provided login credentials and generates a response
     * containing a JWT token along with user details upon successful authentication.
     *
     * @param request the login request containing the user's email and password
     * @return a ResponseEntity containing the authentication response with the generated token,
     *         user details, and any relevant metadata
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Handles the process of changing a user's password. The user needs to provide
     * the current password and a new password that meets the required criteria.
     *
     * @param request the request containing the current password and the new password
     *                to update the user's credentials
     * @return a map containing a message indicating the success of the password change operation
     */
    @PostMapping("/change-password")
    public Map<String, String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return Map.of("message", "Password changed");
    }

    /**
     * Initiates the forgot password process for a user by accepting their email. If the email exists
     * in the system, a reset link is sent to it. This endpoint does not reveal whether the email is
     * valid for security reasons.
     *
     * @param request the forgot password request containing the email of the user who requires a
     *                password reset
     * @return a map containing a message indicating that if the email exists, a reset link has been sent
     */
    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return Map.of("message", "If this email exists, a reset link has been sent.");
    }

    /**
     * Resets the user's password using the provided reset token and new password. This endpoint
     * is typically invoked after the user follows a password reset link sent to their email.
     *
     * @param request the request containing the password reset token and the new password
     *                which must meet the required criteria
     * @return a map containing a message indicating the success of the password reset operation
     */
    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Map.of("message", "Password reset successfully");
    }
}
