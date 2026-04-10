package com.pollflow.service;

import com.pollflow.dto.*;
import com.pollflow.entity.User;
import com.pollflow.entity.User.UserStatus;
import com.pollflow.repository.UserRepository;
import com.pollflow.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final NotificationService notificationService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("An account with this email already exists. Please use a different email or login.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(User.Role.USER)
                .status(UserStatus.PENDING)
                .mobile(request.getMobile())
                .gender(request.getGender())
                .age(request.getAge())
                .region(request.getRegion())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .religion(request.getReligion())
                .address(request.getAddress())
                .maritalStatus(request.getMaritalStatus())
                .build();

        userRepository.save(user);

        notifyVerificationAdmins(user);

        return AuthResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .message("Registration successful. Please wait for admin verification.")
                .build();
    }

    private void notifyVerificationAdmins(User newUser) {
        try {
            List<User> verificationAdmins = userRepository.findByRoleAndStatus(User.Role.VERIFICATION_ADMIN, UserStatus.APPROVED);
            for (User admin : verificationAdmins) {
                try {
                    notificationService.createNotification(
                        admin,
                        "New User Registration",
                        "New user " + newUser.getFullName() + " (" + newUser.getEmail() + ") has registered and is pending approval."
                    );
                } catch (Exception e) {
                    System.out.println("Failed to notify admin: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to get verification admins: " + e.getMessage());
        }
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (user.getStatus() != UserStatus.APPROVED && 
            user.getRole() != User.Role.VERIFICATION_ADMIN && 
            user.getRole() != User.Role.POLL_ADMIN) {
            throw new RuntimeException("Account not approved. Please wait for admin verification.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtProvider.generateToken(org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(new java.util.ArrayList<>())
                .build());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .build();
    }

    public UserDTO getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .profilePicture(user.getProfilePicture())
                .bio(user.getBio())
                .build();
    }
}
