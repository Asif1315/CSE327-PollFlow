package com.pollflow.repository;

import com.pollflow.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Page<Favorite> findByUserId(Long userId, Pageable pageable);
    Optional<Favorite> findByUserIdAndPollId(Long userId, Long pollId);
    boolean existsByUserIdAndPollId(Long userId, Long pollId);
    void deleteByUserIdAndPollId(Long userId, Long pollId);
    List<Favorite> findByPollId(Long pollId);
}
