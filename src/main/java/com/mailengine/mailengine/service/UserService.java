package com.mailengine.mailengine.service;

import com.mailengine.mailengine.dto.request.InviteUserRequest;
import com.mailengine.mailengine.dto.request.UpdateRoleRequest;
import com.mailengine.mailengine.dto.response.UserResponse;
import com.mailengine.mailengine.entity.User;
import com.mailengine.mailengine.entity.enums.Role;
import com.mailengine.mailengine.exception.BadRequestException;
import com.mailengine.mailengine.exception.ConflictException;
import com.mailengine.mailengine.exception.ResourceNotFoundException;
import com.mailengine.mailengine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        User current = getCurrentUser();
        return userRepository.findByAccountId(current.getAccount().getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(findInAccount(id));
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    public UserResponse inviteUser(InviteUserRequest request) {
        User current = getCurrentUser();

        if (current.getRole() != Role.admin) {
            throw new BadRequestException("Only admins can invite users");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("A user with this email already exists");
        }

        // Generate a readable temporary password
        String tempPassword = "Tmp-" + UUID.randomUUID().toString().substring(0, 8);

        User invited = new User();
        invited.setName(request.getName());
        invited.setEmail(request.getEmail());
        invited.setPasswordHash(passwordEncoder.encode(tempPassword));
        invited.setRole(request.getRole());
        invited.setAccount(current.getAccount());
        invited.setInvitedBy(current);
        invited.setEmailVerified(true);   // admin has vouched for this address
        invited.setMustChangePwd(true);

        userRepository.save(invited);

        emailService.sendInvitationEmail(request.getEmail(), tempPassword, current.getName());

        return toResponse(invited);
    }

    public UserResponse updateRole(Long id, UpdateRoleRequest request) {
        User current = getCurrentUser();
        if (current.getRole() != Role.admin) {
            throw new BadRequestException("Only admins can change roles");
        }
        if (current.getId().equals(id)) {
            throw new BadRequestException("You cannot change your own role");
        }
        User target = findInAccount(id);
        target.setRole(request.getRole());
        return toResponse(userRepository.save(target));
    }

    public void deleteUser(Long id) {
        User current = getCurrentUser();
        if (current.getRole() != Role.admin) {
            throw new BadRequestException("Only admins can delete users");
        }
        if (current.getId().equals(id)) {
            throw new BadRequestException("You cannot delete your own account");
        }
        userRepository.delete(findInAccount(id));
    }

    /** Admin-initiated password reset: generates a temp password and emails it. */
    public void adminResetPassword(Long id) {
        User current = getCurrentUser();
        if (current.getRole() != Role.admin) {
            throw new BadRequestException("Only admins can reset user passwords");
        }
        User target = findInAccount(id);

        String tempPassword = "Tmp-" + UUID.randomUUID().toString().substring(0, 8);
        target.setPasswordHash(passwordEncoder.encode(tempPassword));
        target.setMustChangePwd(true);
        userRepository.save(target);

        emailService.sendAdminPasswordResetEmail(target.getEmail(), tempPassword);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /** Loads a user and verifies they belong to the current user's account. */
    private User findInAccount(Long id) {
        User current = getCurrentUser();
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!target.getAccount().getId().equals(current.getAccount().getId())) {
            throw new ResourceNotFoundException("User not found");
        }
        return target;
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getEmailVerified(),
                user.getMustChangePwd(),
                user.getLastLogin(),
                user.getCreatedAt()
        );
    }
}
