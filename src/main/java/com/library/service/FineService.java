package com.library.service;

import com.library.model.Fine;
import com.library.model.FineStatus;
import com.library.model.Reservation;
import com.library.model.User;
import com.library.repository.FineRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FineService
{
    private static final BigDecimal FINE_PER_DAY = BigDecimal.valueOf(5);

    private final FineRepository fineRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public FineService(FineRepository fineRepository,
                       UserRepository userRepository,
                       ReservationRepository reservationRepository)
    {
        this.fineRepository = fineRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<Fine> getAllFines()
    {
        return fineRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Fine getFineById(Long fineId)
    {
        validateId(fineId, "Fine id");

        return fineRepository.findById(fineId)
                .orElseThrow(() -> new RuntimeException("Fine not found with id: " + fineId));
    }

    @Transactional(readOnly = true)
    public List<Fine> getFinesByUserId(Long userId)
    {
        User user = getUserById(userId);

        return fineRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Fine> getFinesByUserIdAndStatus(Long userId, FineStatus status)
    {
        validateFineStatus(status);

        User user = getUserById(userId);

        return fineRepository.findByUserAndStatus(user, status);
    }

    @Transactional(readOnly = true)
    public List<Fine> getFinesByStatus(FineStatus status)
    {
        validateFineStatus(status);

        return fineRepository.findByStatus(status);
    }

    @Transactional
    public Fine createFineForOverdueReturn(Reservation reservation)
    {
        if (reservation == null)
        {
            throw new IllegalArgumentException("Reservation is required");
        }

        if (reservation.getUser() == null)
        {
            throw new IllegalArgumentException("Reservation user is required");
        }

        if (reservation.getExpirationDate() == null)
        {
            return null;
        }

        if (reservation.getReturnDate() == null)
        {
            return null;
        }

        if (!reservation.getReturnDate().isAfter(reservation.getExpirationDate()))
        {
            return null;
        }

        if (fineRepository.existsByReservation(reservation))
        {
            return fineRepository.findByReservation(reservation)
                    .orElse(null);
        }

        long overdueDaysLong = Duration.between(
                reservation.getExpirationDate(),
                reservation.getReturnDate()
        ).toDays();

        if (overdueDaysLong <= 0)
        {
            overdueDaysLong = 1;
        }

        int overdueDays = Math.toIntExact(overdueDaysLong);

        BigDecimal amount = FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays));

        Fine fine = new Fine();
        fine.setUser(reservation.getUser());
        fine.setReservation(reservation);
        fine.setOverdueDays(overdueDays);
        fine.setAmount(amount);
        fine.setStatus(FineStatus.PENDING);
        fine.setReason("Overdue return for book: " + reservation.getBook().getTitle());

        return fineRepository.save(fine);
    }

    @Transactional
    public Fine markFineAsPaid(Long fineId)
    {
        Fine fine = getFineById(fineId);

        if (fine.getStatus() == FineStatus.PAID)
        {
            throw new IllegalStateException("Fine is already paid");
        }

        if (fine.getStatus() == FineStatus.CANCELLED)
        {
            throw new IllegalStateException("Cancelled fine cannot be paid");
        }

        fine.setStatus(FineStatus.PAID);
        fine.setPaidAt(LocalDateTime.now());

        return fineRepository.save(fine);
    }

    @Transactional
    public Fine cancelFine(Long fineId)
    {
        Fine fine = getFineById(fineId);

        if (fine.getStatus() == FineStatus.PAID)
        {
            throw new IllegalStateException("Paid fine cannot be cancelled");
        }

        if (fine.getStatus() == FineStatus.CANCELLED)
        {
            throw new IllegalStateException("Fine is already cancelled");
        }

        fine.setStatus(FineStatus.CANCELLED);

        return fineRepository.save(fine);
    }

    @Transactional
    public void deleteFine(Long fineId)
    {
        Fine fine = getFineById(fineId);

        if (fine.getStatus() == FineStatus.PAID)
        {
            throw new IllegalStateException("Paid fine cannot be deleted");
        }

        fineRepository.delete(fine);
    }

    private User getUserById(Long userId)
    {
        validateId(userId, "User id");

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    private void validateId(Long id, String fieldName)
    {
        if (id == null)
        {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        if (id <= 0)
        {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
    }

    private void validateFineStatus(FineStatus status)
    {
        if (status == null)
        {
            throw new IllegalArgumentException("Fine status is required");
        }
    }
}