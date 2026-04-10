package com.pollflow.repository;

import com.pollflow.entity.User;
import com.pollflow.entity.User.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByStatus(UserStatus status);
    long countByStatus(UserStatus status);
    
    @Query("SELECT u FROM User u WHERE u.email LIKE %:query% OR u.fullName LIKE %:query%")
    List<User> searchUsers(String query);
    
    List<User> findByRole(User.Role role);
    
    List<User> findByRoleAndStatus(User.Role role, UserStatus status);
}
