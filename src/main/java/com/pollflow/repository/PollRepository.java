package com.pollflow.repository;

import com.pollflow.entity.Poll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {
    Page<Poll> findByIsDeletedFalse(Pageable pageable);
    
    @Query("SELECT p FROM Poll p WHERE p.isDeleted = false AND p.category.id = :categoryId")
    Page<Poll> findByCategoryAndIsDeletedFalse(@Param("categoryId") Long categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Poll p WHERE p.isDeleted = false AND p.createdBy.id = :userId")
    Page<Poll> findByCreatedByIdAndIsDeletedFalse(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Poll p WHERE p.isDeleted = false")
    long countActivePolls();
    
    List<Poll> findByIsDeletedFalse();
    
    @Query("SELECT p FROM Poll p WHERE p.isDeleted = false AND (p.title LIKE %:query% OR p.description LIKE %:query%)")
    List<Poll> searchPolls(@Param("query") String query);
    
    @Query("SELECT p FROM Poll p WHERE p.isDeleted = false AND p.category.id = :categoryId")
    List<Poll> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT p FROM Poll p WHERE p.isDeleted = false AND p.pollType = :pollType")
    List<Poll> findByPollType(@Param("pollType") Poll.PollType pollType);
    
    @Query("SELECT p FROM Poll p WHERE p.isDeleted = false AND p.category.id = :categoryId AND p.pollType = :pollType")
    List<Poll> findByCategoryIdAndPollType(@Param("categoryId") Long categoryId, @Param("pollType") Poll.PollType pollType);
    
    @Query("SELECT p FROM Poll p WHERE p.isDeleted = false AND (p.title LIKE %:query% OR p.description LIKE %:query%)")
    List<Poll> searchPollsWithCategory(@Param("query") String query);
    
    @Query("SELECT p FROM Poll p WHERE p.isDeleted = false AND p.category.id = :categoryId AND (p.title LIKE %:query% OR p.description LIKE %:query%)")
    List<Poll> searchPollsByCategory(@Param("query") String query, @Param("categoryId") Long categoryId);
    
    @Query("SELECT p FROM Poll p WHERE p.pollType = 'TIME_BASED' AND p.endTime < :endTime")
    List<Poll> findEndedTimeBasedPolls(@Param("endTime") java.time.LocalDateTime endTime);
}
