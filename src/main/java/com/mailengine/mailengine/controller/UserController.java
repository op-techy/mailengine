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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/invite")
    public ResponseEntity<UserResponse> inviteUser(@Valid @RequestBody InviteUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.inviteUser(request));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(userService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /** Admin-initiated password reset — generates temp password and emails it. */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<MessageResponse> adminResetPassword(@PathVariable Long id) {
        userService.adminResetPassword(id);
        return ResponseEntity.ok(new MessageResponse("Temporary password sent to user's email"));
    }
}
