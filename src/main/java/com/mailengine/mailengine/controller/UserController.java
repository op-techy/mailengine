package com.mailengine.mailengine.controller;

import com.mailengine.mailengine.dto.request.InviteUserRequest;
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

    @PostMapping("/invite")
    public ResponseEntity<UserResponse> inviteUser(@Valid @RequestBody InviteUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.inviteUser(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
