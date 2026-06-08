package com.library.repository;

import com.library.model.Challenge;
import com.library.model.ChallengeParticipation;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {
    List<ChallengeParticipation> findByUser(User user);
    Optional<ChallengeParticipation> findByUserAndChallenge(User user, Challenge challenge);
    boolean existsByUserAndChallenge(User user, Challenge challenge);
    long countByChallenge(Challenge challenge);
}