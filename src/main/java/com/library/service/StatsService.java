package com.library.service;

import com.library.model.BookCopyStatus;
import com.library.model.Fine;
import com.library.model.FineStatus;
import com.library.model.ReservationStatus;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.DownloadHistoryRepository;
import com.library.repository.FineRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService
{
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookCopyRepository bookCopyRepository;
    private final ReservationRepository reservationRepository;
    private final DownloadHistoryRepository downloadHistoryRepository;
    private final FineRepository fineRepository;

    public StatsService(BookRepository bookRepository,
                        UserRepository userRepository,
                        BookCopyRepository bookCopyRepository,
                        ReservationRepository reservationRepository,
                        DownloadHistoryRepository downloadHistoryRepository,
                        FineRepository fineRepository)
    {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.reservationRepository = reservationRepository;
        this.downloadHistoryRepository = downloadHistoryRepository;
        this.fineRepository = fineRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getGeneralStats()
    {
        Map<String, Object> stats = new HashMap<>();

        long totalCopies = bookCopyRepository.count();

        long availableCopies = bookCopyRepository.findAll().stream()
                .filter(copy -> copy.getStatus() == BookCopyStatus.AVAILABLE)
                .count();

        List<ReservationStatus> activeStatuses = List.of(
                ReservationStatus.CREATED,
                ReservationStatus.CONFIRMED,
                ReservationStatus.WAITING,
                ReservationStatus.READY_FOR_PICKUP,
                ReservationStatus.BORROWED
        );

        long activeReservations = reservationRepository.findAll().stream()
                .filter(reservation -> activeStatuses.contains(reservation.getStatus()))
                .count();

        long digitalBooks = bookRepository.findByHasDigitalCopyTrue().size();

        long totalDownloads = downloadHistoryRepository.count();

        long totalFines = fineRepository.count();

        long pendingFines = fineRepository.findByStatus(FineStatus.PENDING).size();
        long paidFines = fineRepository.findByStatus(FineStatus.PAID).size();
        long cancelledFines = fineRepository.findByStatus(FineStatus.CANCELLED).size();

        BigDecimal totalFineAmount = fineRepository.findAll().stream()
                .map(Fine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingFineAmount = fineRepository.findByStatus(FineStatus.PENDING).stream()
                .map(Fine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalBooks", bookRepository.count());
        stats.put("totalUsers", userRepository.count());
        stats.put("totalCopies", totalCopies);
        stats.put("availableCopies", availableCopies);
        stats.put("digitalBooks", digitalBooks);

        stats.put("totalReservations", reservationRepository.count());
        stats.put("activeReservations", activeReservations);

        stats.put("totalDownloads", totalDownloads);

        stats.put("totalFines", totalFines);
        stats.put("pendingFines", pendingFines);
        stats.put("paidFines", paidFines);
        stats.put("cancelledFines", cancelledFines);
        stats.put("totalFineAmount", totalFineAmount);
        stats.put("pendingFineAmount", pendingFineAmount);

        return stats;
    }
}