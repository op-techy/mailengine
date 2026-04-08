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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Handles the registration of a new user and their associated account. This endpoint
     * creates a user, associates them with a company account, and sends an email verification
     * link to the provided email address.
     *
     * @param request the registration request containing user details such as name, email,
     *                password, and company name
     * @return a ResponseEntity containing a message indicating successful account creation
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Account created. Check your email to verify."));
    }

    /**
     * Verifies the user's email address using the provided token. This endpoint finalizes
     * the email verification process, enabling the user to log in upon successful verification.
     *
     * @param token the email verification token used to validate and confirm the user's email address
     * @return a ResponseEntity containing a message indicating successful email verification
     */
    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(new MessageResponse("Email verified. You can now log in."));
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Handles the password change request for the authenticated user. Validates the current
     * password provided in the request and updates it to the new password if the validation is successful.
     *
     * @param request the request object containing the current password and the new password
     * @return a ResponseEntity containing a message indicating successful password change
     */
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    /**
     * Handles the forgot password request by initiating the process to send a password reset link
     * to the provided email address if it exists in the system.
     *
     * @param request the forgot password request containing the user's email
     * @return a ResponseEntity containing a message indicating that a reset link has been sent
     *         if the email exists
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(
                new MessageResponse("If this email exists, a reset link has been sent."));
    }

    /**
     * Handles the reset password request by updating the user's password based on the provided
     * reset password token and new password. Validates the reset credentials and updates the
     * password if the validation is successful.
     *
     * @param request the reset password request containing the reset token and new password
     * @return a ResponseEntity containing a message indicating successful password reset
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponse("Password reset successfully. You can now log in."));
    }
}
