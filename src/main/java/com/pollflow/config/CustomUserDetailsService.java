package com.pollflow.config;

import com.pollflow.entity.User;
import com.pollflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        // Check status and give appropriate message
        if (user.getStatus() == User.UserStatus.PENDING) {
            throw new UsernameNotFoundException("Account is under verification. Please wait for admin approval.");
        }
        
        if (user.getStatus() == User.UserStatus.REJECTED) {
            throw new UsernameNotFoundException("Account rejected. Reason: " + (user.getRejectionReason() != null ? user.getRejectionReason() : "Contact admin for details"));
        }
        
        // Allow admins regardless of status, regular users must be APPROVED
        if (user.getStatus() != User.UserStatus.APPROVED && 
            user.getRole() != User.Role.VERIFICATION_ADMIN && 
            user.getRole() != User.Role.POLL_ADMIN) {
            throw new UsernameNotFoundException("Account not approved. Please wait for admin verification.");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
