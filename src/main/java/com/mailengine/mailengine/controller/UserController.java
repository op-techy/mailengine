package com.mailengine.mailengine.controller;

import com.mailengine.mailengine.dto.request.InviteUserRequest;
import com.mailengine.mailengine.dto.request.UpdateRoleRequest;
import com.mailengine.mailengine.dto.response.MessageResponse;
import com.mailengine.mailengine.dto.response.UserResponse;
import com.mailengine.mailengine.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Retrieves a list of users associated with the current account.
     *
     * @return a {@code ResponseEntity} containing a list of {@code UserResponse} objects,
     * which represent the details of each user including ID, name, email, role, email verification status,
     * password change requirement, last login timestamp, and creation timestamp.
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    /**
     * Invites a new user to the system by creating an account for them and sending an invitation email.
     *
     * @param request the {@code InviteUserRequest} object containing the details of the user to be invited,
     *                including name, email, and role.
     * @return a {@code ResponseEntity} containing a {@code Map} with the user details, including:
     *         - "id": the unique identifier of the invited user
     *         - "name": the name of the invited user
     *         - "email": the email address of the invited user
     *         - "role": the assigned role of the invited user
     */
    @PostMapping("/invite")
    public ResponseEntity<Map<String, Object>> inviteUser(@Valid @RequestBody InviteUserRequest request) {
       UserResponse user = userService.inviteUser(request);
       return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
               "id", user.getId(),
               "name", user.getName(),
               "email", user.getEmail(),
               "role", user.getRole()
       ));
    }

    /**
     * Updates the role of a user with the specified ID.
     *
     * @param id the unique identifier of the user whose role is to be updated
     * @param request the {@code UpdateRoleRequest} object containing the new role to be assigned to the user
     * @return a {@code ResponseEntity} containing a {@code UserResponse} object with the updated user details,
     *         including ID, name, email, role, email verification status, password change requirement,
     *         last login timestamp, and creation timestamp
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(userService.updateRole(id, request));
    }

    /**
     * Deletes a user with the specified ID from the system.
     * This operation is restricted to admin users and ensures that an admin
     * cannot delete their own account.
     *
     * @param id the unique identifier of the user to be deleted
     * @return a {@code ResponseEntity} with no content if the user is successfully deleted
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Resets a user's password by generating a temporary password and sending it to the user's email.
     * This operation can only be performed by an admin.
     *
     * @param id the unique identifier of the user whose password is to be reset
     * @return a {@code ResponseEntity} containing a {@code Map} with a success message
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, String>> adminResetPassword(@PathVariable Long id) {
        userService.adminResetPassword(id);
        return ResponseEntity.ok(Map.of("message","Temporary password sent to user's email"));
    }
}
