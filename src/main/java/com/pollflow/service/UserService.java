package com.pollflow.service;

import com.pollflow.dto.UserDTO;
import com.pollflow.entity.User;
import com.pollflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDTO getProfile() {
        User user = getCurrentUser();
        return mapToDTO(user);
    }

    @Transactional
    public UserDTO updateProfile(UserDTO updatedDTO) {
        User user = getCurrentUser();
        
        if (updatedDTO.getFullName() != null) {
            user.setFullName(updatedDTO.getFullName());
        }
        if (updatedDTO.getProfilePicture() != null) {
            user.setProfilePicture(updatedDTO.getProfilePicture());
        }
        if (updatedDTO.getBio() != null) {
            user.setBio(updatedDTO.getBio());
        }
        
        userRepository.save(user);
        return mapToDTO(user);
    }
    
    @Transactional
    public void updateProfile(String fullName, String mobile, String gender, Integer age, 
                               String region, String city, String postalCode, String religion, String address,
                               String maritalStatus) {
        User user = getCurrentUser();
        if (fullName != null) user.setFullName(fullName);
        if (mobile != null) user.setMobile(mobile);
        if (gender != null) user.setGender(gender);
        if (age != null) user.setAge(age);
        if (region != null) user.setRegion(region);
        if (city != null) user.setCity(city);
        if (postalCode != null) user.setPostalCode(postalCode);
        if (religion != null) user.setReligion(religion);
        if (address != null) user.setAddress(address);
        if (maritalStatus != null) user.setMaritalStatus(maritalStatus);
        userRepository.save(user);
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .rejectionReason(user.getRejectionReason())
                .profilePicture(user.getProfilePicture())
                .bio(user.getBio())
                .mobile(user.getMobile())
                .gender(user.getGender())
                .age(user.getAge())
                .region(user.getRegion())
                .city(user.getCity())
                .postalCode(user.getPostalCode())
                .religion(user.getReligion())
                .address(user.getAddress())
                .maritalStatus(user.getMaritalStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
