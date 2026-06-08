package com.library.service;

import com.library.dto.ChallengeRequest;
import com.library.dto.ChallengeResponse;
import com.library.model.*;
import com.library.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChallengeService {
    private final ChallengeRepository challengeRepo;
    private final ChallengeParticipationRepository participationRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final AuthorRepository authorRepo;
    private final DownloadHistoryRepository downloadRepo;
    private final ReservationRepository reservationRepo;
    private final ReviewRepository reviewRepo;

    public ChallengeService(ChallengeRepository challengeRepo,
                            ChallengeParticipationRepository participationRepo,
                            UserRepository userRepo,
                            CategoryRepository categoryRepo,
                            AuthorRepository authorRepo,
                            DownloadHistoryRepository downloadRepo,
                            ReservationRepository reservationRepo,
                            ReviewRepository reviewRepo) {
        this.challengeRepo = challengeRepo;
        this.participationRepo = participationRepo;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
        this.authorRepo = authorRepo;
        this.downloadRepo = downloadRepo;
        this.reservationRepo = reservationRepo;
        this.reviewRepo = reviewRepo;
    }

    /* ========= CRUD ADMIN ========= */
    @Transactional
    public ChallengeResponse create(ChallengeRequest req) {
        validate(req);
        Challenge c = new Challenge();
        applyRequest(c, req);
        c.setActive(true);
        return toResponse(challengeRepo.save(c), null);
    }

    @Transactional
    public ChallengeResponse update(Long id, ChallengeRequest req) {
        validate(req);
        Challenge c = challengeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));
        applyRequest(c, req);
        return toResponse(challengeRepo.save(c), null);
    }

    @Transactional
    public void archive(Long id) {
        Challenge c = challengeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));
        c.setActive(false);
        challengeRepo.save(c);
    }

    /* ========= LISTĂRI ========= */
    @Transactional(readOnly = true)
    public List<ChallengeResponse> listActive(User currentUser) {
        return challengeRepo.findByActiveTrueOrderByCreatedAtDesc().stream()
                .map(c -> toResponse(c, currentUser))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChallengeResponse> myChallenges(User user) {
        return participationRepo.findByUser(user).stream()
                .map(p -> toResponse(p.getChallenge(), user))
                .toList();
    }

    /* ========= JOIN / LEAVE ========= */
    @Transactional
    public ChallengeResponse join(Long challengeId, User user) {
        Challenge c = challengeRepo.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));
        if (!c.isActive()) throw new RuntimeException("Challenge inactive");
        if (LocalDateTime.now().isAfter(c.getEndDate()))
            throw new RuntimeException("Challenge already ended");

        if (participationRepo.existsByUserAndChallenge(user, c))
            throw new RuntimeException("Already joined");

        ChallengeParticipation p = new ChallengeParticipation();
        p.setUser(user);
        p.setChallenge(c);
        p.setStatus(ParticipationStatus.IN_PROGRESS);
        participationRepo.save(p);
        return toResponse(c, user);
    }

    @Transactional
    public void leave(Long challengeId, User user) {
        Challenge c = challengeRepo.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));
        ChallengeParticipation p = participationRepo.findByUserAndChallenge(user, c)
                .orElseThrow(() -> new RuntimeException("Not joined"));
        // dacă deja a fost completat, doar marchează "abandoned" e ciudat. Eliminăm participarea.
        participationRepo.delete(p);
    }

    /* ========= PROGRES ========= */
    private int calculateProgress(Challenge c, User user, LocalDateTime joinedAt) {
        LocalDateTime from = joinedAt.isAfter(c.getStartDate()) ? joinedAt : c.getStartDate();
        LocalDateTime to = c.getEndDate();
        Set<Long> uniqueBookIds = new HashSet<>();
        int count = 0;

        switch (c.getType()) {
            case READ_FROM_CATEGORY -> {
                if (c.getCategoryId() == null) return 0;
                downloadRepo.findByUserOrderByDownloadDateDesc(user).stream()
                        .filter(d -> inWindow(d.getDownloadDate(), from, to))
                        .filter(d -> d.getBook() != null && d.getBook().getCategory() != null
                                && c.getCategoryId().equals(d.getBook().getCategory().getCategoryId()))
                        .forEach(d -> uniqueBookIds.add(d.getBook().getBookId()));
                reservationRepo.findByUser(user).stream()
                        .filter(r -> inWindow(r.getReservationDate(), from, to))
                        .filter(r -> r.getBook() != null && r.getBook().getCategory() != null
                                && c.getCategoryId().equals(r.getBook().getCategory().getCategoryId()))
                        .forEach(r -> uniqueBookIds.add(r.getBook().getBookId()));
                count = uniqueBookIds.size();
            }
            case READ_FROM_AUTHOR -> {
                if (c.getAuthorId() == null) return 0;
                downloadRepo.findByUserOrderByDownloadDateDesc(user).stream()
                        .filter(d -> inWindow(d.getDownloadDate(), from, to))
                        .filter(d -> d.getBook() != null && d.getBook().getAuthors() != null
                                && d.getBook().getAuthors().stream()
                                .anyMatch(a -> c.getAuthorId().equals(a.getAuthorId())))
                        .forEach(d -> uniqueBookIds.add(d.getBook().getBookId()));
                reservationRepo.findByUser(user).stream()
                        .filter(r -> inWindow(r.getReservationDate(), from, to))
                        .filter(r -> r.getBook() != null && r.getBook().getAuthors() != null
                                && r.getBook().getAuthors().stream()
                                .anyMatch(a -> c.getAuthorId().equals(a.getAuthorId())))
                        .forEach(r -> uniqueBookIds.add(r.getBook().getBookId()));
                count = uniqueBookIds.size();
            }
            case READ_DIGITAL -> {
                downloadRepo.findByUserOrderByDownloadDateDesc(user).stream()
                        .filter(d -> inWindow(d.getDownloadDate(), from, to))
                        .filter(d -> d.getBook() != null)
                        .forEach(d -> uniqueBookIds.add(d.getBook().getBookId()));
                count = uniqueBookIds.size();
            }
            case READ_PHYSICAL -> {
                reservationRepo.findByUser(user).stream()
                        .filter(r -> inWindow(r.getReservationDate(), from, to))
                        .filter(r -> r.getBook() != null)
                        .forEach(r -> uniqueBookIds.add(r.getBook().getBookId()));
                count = uniqueBookIds.size();
            }
            case WRITE_REVIEWS -> {
                count = (int) reviewRepo.findByUser(user).stream()
                        .filter(rv -> inWindow(rv.getCreatedAt(), from, to))
                        .count();
            }
            case ANY_READ -> {
                downloadRepo.findByUserOrderByDownloadDateDesc(user).stream()
                        .filter(d -> inWindow(d.getDownloadDate(), from, to))
                        .filter(d -> d.getBook() != null)
                        .forEach(d -> uniqueBookIds.add(d.getBook().getBookId()));
                reservationRepo.findByUser(user).stream()
                        .filter(r -> inWindow(r.getReservationDate(), from, to))
                        .filter(r -> r.getBook() != null)
                        .forEach(r -> uniqueBookIds.add(r.getBook().getBookId()));
                count = uniqueBookIds.size();
            }
        }
        return count;
    }

    private boolean inWindow(LocalDateTime t, LocalDateTime from, LocalDateTime to) {
        return t != null && !t.isBefore(from) && !t.isAfter(to);
    }

    /* ========= MAPPING ========= */
    private ChallengeResponse toResponse(Challenge c, User user) {
        ChallengeResponse r = new ChallengeResponse();
        r.setChallengeId(c.getChallengeId());
        r.setTitle(c.getTitle());
        r.setDescription(c.getDescription());
        r.setType(c.getType());
        r.setTargetCount(c.getTargetCount());
        r.setCategoryId(c.getCategoryId());
        r.setAuthorId(c.getAuthorId());
        r.setStartDate(c.getStartDate());
        r.setEndDate(c.getEndDate());
        r.setIconEmoji(c.getIconEmoji());
        r.setActive(c.isActive());
        r.setParticipantsCount(participationRepo.countByChallenge(c));

        if (c.getCategoryId() != null) {
            categoryRepo.findById(c.getCategoryId())
                    .ifPresent(cat -> r.setCategoryName(cat.getName()));
        }
        if (c.getAuthorId() != null) {
            authorRepo.findById(c.getAuthorId())
                    .ifPresent(a -> r.setAuthorName(a.getName()));
        }

        if (user != null) {
            Optional<ChallengeParticipation> opt = participationRepo.findByUserAndChallenge(user, c);
            if (opt.isPresent()) {
                ChallengeParticipation p = opt.get();
                r.setJoined(true);
                r.setUserJoinedAt(p.getJoinedAt());
                r.setUserCompletedAt(p.getCompletedAt());

                int progress = calculateProgress(c, user, p.getJoinedAt());
                progress = Math.min(progress, c.getTargetCount());
                r.setUserProgress(progress);

                // Auto-mark COMPLETED dacă a atins target
                if (progress >= c.getTargetCount()
                        && p.getStatus() == ParticipationStatus.IN_PROGRESS) {
                    p.setStatus(ParticipationStatus.COMPLETED);
                    p.setCompletedAt(LocalDateTime.now());
                    participationRepo.save(p);
                    r.setUserCompletedAt(p.getCompletedAt());
                }
                r.setUserStatus(p.getStatus().name());
            } else {
                r.setJoined(false);
                r.setUserProgress(0);
            }
        }

        return r;
    }

    private void applyRequest(Challenge c, ChallengeRequest req) {
        c.setTitle(req.getTitle().trim());
        c.setDescription(req.getDescription());
        c.setType(req.getType());
        c.setTargetCount(req.getTargetCount());
        c.setCategoryId(req.getCategoryId());
        c.setAuthorId(req.getAuthorId());
        c.setStartDate(req.getStartDate());
        c.setEndDate(req.getEndDate());
        c.setIconEmoji(req.getIconEmoji());
    }

    private void validate(ChallengeRequest req) {
        if (req == null) throw new RuntimeException("Request cannot be null");
        if (req.getTitle() == null || req.getTitle().trim().isEmpty())
            throw new RuntimeException("Title is required");
        if (req.getType() == null) throw new RuntimeException("Type is required");
        if (req.getTargetCount() <= 0) throw new RuntimeException("Target count must be > 0");
        if (req.getStartDate() == null || req.getEndDate() == null)
            throw new RuntimeException("Start and end dates required");
        if (req.getEndDate().isBefore(req.getStartDate()))
            throw new RuntimeException("End date must be after start date");
        if (req.getType() == ChallengeType.READ_FROM_CATEGORY && req.getCategoryId() == null)
            throw new RuntimeException("Category required for this type");
        if (req.getType() == ChallengeType.READ_FROM_AUTHOR && req.getAuthorId() == null)
            throw new RuntimeException("Author required for this type");
    }
}