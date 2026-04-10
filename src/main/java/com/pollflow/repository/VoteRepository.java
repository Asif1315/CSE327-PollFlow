package com.pollflow.repository;

import com.pollflow.entity.User;
import com.pollflow.entity.Vote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByPollIdAndUserId(Long pollId, Long userId);
    boolean existsByPollIdAndUserId(Long pollId, Long userId);
    long countByPollId(Long pollId);
    Page<Vote> findByUserIdOrderByVotedAtDesc(Long userId, Pageable pageable);
    
    @Query("SELECT DISTINCT v.user FROM Vote v WHERE v.poll.id = :pollId")
    List<User> findDistinctUserByPollId(Long pollId);
}
