package com.pollflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private String status;
    private String rejectionReason;
    private String profilePicture;
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
    private LocalDateTime createdAt;
}
