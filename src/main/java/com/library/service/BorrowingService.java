package com.library.service;

import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.BookCopyStatus;
import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import com.library.repository.BookCopyRepository;
import com.library.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BorrowingService
{
    private static final int PICKUP_DAYS_LIMIT = 2;

    private final ReservationRepository reservationRepository;
    private final BookCopyRepository bookCopyRepository;

    public BorrowingService(ReservationRepository reservationRepository,
                            BookCopyRepository bookCopyRepository)
    {
        this.reservationRepository = reservationRepository;
        this.bookCopyRepository = bookCopyRepository;
    }

    @Transactional
    public Reservation borrowBook(Long reservationId)
    {
        validateId(reservationId);

        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() != ReservationStatus.READY_FOR_PICKUP)
        {
            throw new RuntimeException("Only reservations ready for pickup can be borrowed");
        }

        BookCopy bookCopy = reservation.getBookCopy();

        if (bookCopy == null)
        {
            throw new RuntimeException("This reservation does not have a physical copy assigned");
        }

        if (bookCopy.getStatus() != BookCopyStatus.READY_FOR_PICKUP)
        {
            throw new RuntimeException("Book copy is not ready for pickup");
        }

        reservation.setStatus(ReservationStatus.BORROWED);
        reservation.setPickupDate(LocalDateTime.now());

        bookCopy.setStatus(BookCopyStatus.BORROWED);

        bookCopyRepository.save(bookCopy);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation returnBook(Long reservationId)
    {
        validateId(reservationId);

        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() != ReservationStatus.BORROWED)
        {
            throw new RuntimeException("Only borrowed reservations can be returned");
        }

        BookCopy bookCopy = reservation.getBookCopy();

        if (bookCopy == null)
        {
            throw new RuntimeException("This reservation does not have a physical copy assigned");
        }

        if (bookCopy.getStatus() != BookCopyStatus.BORROWED)
        {
            throw new RuntimeException("Book copy is not marked as borrowed");
        }

        reservation.setReturnDate(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.RETURNED);

        Reservation nextWaitingReservation = getNextWaitingReservation(reservation.getBook());

        if (nextWaitingReservation != null)
        {
            nextWaitingReservation.setBookCopy(bookCopy);
            nextWaitingReservation.setStatus(ReservationStatus.CONFIRMED);
            nextWaitingReservation.setExpirationDate(LocalDateTime.now().plusDays(PICKUP_DAYS_LIMIT));

            bookCopy.setStatus(BookCopyStatus.RESERVED);

            reservationRepository.save(nextWaitingReservation);
        }
        else
        {
            bookCopy.setStatus(BookCopyStatus.AVAILABLE);
        }

        bookCopyRepository.save(bookCopy);

        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getBorrowedReservations()
    {
        return reservationRepository.findByStatus(ReservationStatus.BORROWED);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReturnedReservations()
    {
        return reservationRepository.findByStatus(ReservationStatus.RETURNED);
    }

    private Reservation getReservationById(Long reservationId)
    {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));
    }

    private Reservation getNextWaitingReservation(Book book)
    {
        List<Reservation> waitingReservations =
                reservationRepository.findByBookAndStatusOrderByReservationDateAsc(book, ReservationStatus.WAITING);

        if (waitingReservations.isEmpty())
        {
            return null;
        }

        return waitingReservations.get(0);
    }

    private void validateId(Long id)
    {
        if (id == null || id <= 0)
        {
            throw new RuntimeException("Invalid id");
        }
    }
}