package com.pollflow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    private String rejectionReason;

    private String profilePicture;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String mobile;
    
    private String gender;
    
    private Integer age;
    
    private String region;
    
    private String city;
    
    private String postalCode;
    
    private String religion;
    
    private String address;
    
    private String maritalStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Role {
        USER, POLL_ADMIN, VERIFICATION_ADMIN
    }

    public enum UserStatus {
        PENDING, APPROVED, REJECTED
    }
}
