package com.library.repository;
import com.library.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GroupBookVoteRepository extends JpaRepository<GroupBookVote, Long> {
    Optional<GroupBookVote> findByGroupAndUserAndPeriod(BookGroup g, User u, String period);
    List<GroupBookVote> findByGroupAndPeriod(BookGroup g, String period);
}