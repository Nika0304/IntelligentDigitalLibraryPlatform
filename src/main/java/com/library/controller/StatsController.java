package com.library.controller;

import com.library.model.BookCopyStatus;
import com.library.model.ReservationStatus;
import com.library.repository.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController
{
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookCopyRepository bookCopyRepository;
    private final ReservationRepository reservationRepository;

    public StatsController(BookRepository bookRepository,
                           UserRepository userRepository,
                           BookCopyRepository bookCopyRepository,
                           ReservationRepository reservationRepository)
    {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.reservationRepository = reservationRepository;
    }

    @GetMapping
    public Map<String, Object> getStats()
    {
        Map<String, Object> stats = new HashMap<>();

        long totalCopies = bookCopyRepository.count();
        long availableCopies = bookCopyRepository.findAll().stream()
                .filter(c -> c.getStatus() == BookCopyStatus.AVAILABLE)
                .count();

        List<ReservationStatus> activeStatuses = List.of(
                ReservationStatus.CREATED,
                ReservationStatus.CONFIRMED,
                ReservationStatus.WAITING,
                ReservationStatus.READY_FOR_PICKUP,
                ReservationStatus.BORROWED
        );

        long activeReservations = reservationRepository.findAll().stream()
                .filter(r -> activeStatuses.contains(r.getStatus()))
                .count();

        long digitalBooks = bookRepository.findByHasDigitalCopyTrue().size();

        stats.put("totalBooks", bookRepository.count());
        stats.put("totalUsers", userRepository.count());
        stats.put("totalCopies", totalCopies);
        stats.put("availableCopies", availableCopies);
        stats.put("activeReservations", activeReservations);
        stats.put("totalReservations", reservationRepository.count());
        stats.put("digitalBooks", digitalBooks);

        return stats;
    }
}
