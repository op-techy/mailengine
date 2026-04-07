package com.mailengine.mailengine.service;

import com.mailengine.mailengine.dto.request.InviteUserRequest;
import com.mailengine.mailengine.dto.response.UserResponse;
import com.mailengine.mailengine.entity.User;
import com.mailengine.mailengine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return the authenticated {@code User} object associated with the current session
     */
    private User getCurrentUser(){
        return (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    /**
     * Retrieves all users associated with the account of the currently authenticated user.
     *
     * @return a list of {@code UserResponse} objects representing the users within the account
     */
    public List<UserResponse> getUsers(){
        User currentUser = getCurrentUser();
        return userRepository.findByAccountId(currentUser.getAccount().getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id the unique identifier of the user to retrieve
     * @return a {@code UserResponse} object containing the details of the user
     * @throws RuntimeException if no user is found with the specified identifier
     */
    public UserResponse getUserById(long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponse(user);
    }

    /**
     * Invites a new user to the system by creating a user record with the provided details
     * and associating it with the account of the currently authenticated user.
     * The invitee will be required to change their password on first login.
     *
     * @param request an {@code InviteUserRequest} containing the email and role of the user to be invited
     * @return a {@code UserResponse} object representing the newly created user
     * @throws RuntimeException if a user with the specified email already exists in the system
     */
    public UserResponse inviteUser(InviteUserRequest request){
        User currentUser = getCurrentUser();

        if (userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already in use");
        }

        User invited = new User();
        invited.setName(request.getEmail()); // name unknown at invite time
        invited.setEmail(request.getEmail());
        invited.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        invited.setRole(request.getRole());
        invited.setAccount(currentUser.getAccount());
        invited.setInvitedBy(currentUser);
        invited.setMustChangePwd(true); // force password change on first login

        userRepository.save(invited);
        return toResponse(invited);
    }

    /**
     * Deletes a user with the specified unique identifier.
     *
     * @param id the unique identifier of the user to delete
     * @throws RuntimeException if no user is found with the specified identifier
     */
    public void deleteUser(long id){
        User user = userRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    /**
     * Converts a {@code User} entity object into a {@code UserResponse} DTO object.
     *
     * @param user the {@code User} entity to be converted
     * @return a {@code UserResponse} object containing the mapped details of the given {@code User} entity
     */
    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getEmailVerified(),
                user.getMustChangePwd(),
                user.getCreatedAt()
        );
    }
}
