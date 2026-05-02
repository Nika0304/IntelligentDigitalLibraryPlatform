package com.library.service;

import com.library.dto.ReservationRequest;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.BookCopyStatus;
import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import com.library.model.User;
import com.library.model.UserStatus;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService
{
    private static final int PICKUP_DAYS_LIMIT = 2;
    private static final int MAX_ACTIVE_RESERVATIONS = 5;

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              UserRepository userRepository,
                              BookRepository bookRepository,
                              BookCopyRepository bookCopyRepository)
    {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
    }

    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations()
    {
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Reservation getReservationById(Long reservationId)
    {
        validateId(reservationId);

        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByUserId(Long userId)
    {
        User user = getUserById(userId);

        return reservationRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByBookId(Long bookId)
    {
        Book book = getBookById(bookId);

        return reservationRepository.findByBook(book);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByStatus(String statusText)
    {
        ReservationStatus status = parseReservationStatus(statusText);

        return reservationRepository.findByStatus(status);
    }

    @Transactional
    public Reservation createReservation(ReservationRequest request)
    {
        validateReservationRequest(request);

        User user = getUserById(request.getUserId());

        if (user.getStatus() != UserStatus.ACTIVE)
        {
            throw new RuntimeException("Only active users can make reservations");
        }

        Book book = getBookById(request.getBookId());

        List<BookCopy> bookCopies = bookCopyRepository.findByBook(book);

        if (bookCopies.isEmpty())
        {
            throw new RuntimeException("Book has no physical copies available for reservation");
        }

        checkActiveReservationLimit(user);
        checkDuplicateActiveReservation(user, book);

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setReservationDate(LocalDateTime.now());

        BookCopy availableCopy = bookCopyRepository
                .findFirstByBookAndStatus(book, BookCopyStatus.AVAILABLE)
                .orElse(null);

        if (availableCopy != null)
        {
            reservation.setBookCopy(availableCopy);
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setExpirationDate(LocalDateTime.now().plusDays(PICKUP_DAYS_LIMIT));

            availableCopy.setStatus(BookCopyStatus.RESERVED);
            bookCopyRepository.save(availableCopy);
        }
        else
        {
            reservation.setStatus(ReservationStatus.WAITING);
        }

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation markReadyForPickup(Long reservationId)
    {
        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() != ReservationStatus.CONFIRMED)
        {
            throw new RuntimeException("Only confirmed reservations can be marked as ready for pickup");
        }

        BookCopy bookCopy = reservation.getBookCopy();

        if (bookCopy == null)
        {
            throw new RuntimeException("This reservation does not have a physical copy assigned");
        }

        if (bookCopy.getStatus() != BookCopyStatus.RESERVED)
        {
            throw new RuntimeException("Book copy must be reserved before it can be marked as ready for pickup");
        }

        reservation.setStatus(ReservationStatus.READY_FOR_PICKUP);
        reservation.setExpirationDate(LocalDateTime.now().plusDays(PICKUP_DAYS_LIMIT));

        bookCopy.setStatus(BookCopyStatus.READY_FOR_PICKUP);
        bookCopyRepository.save(bookCopy);

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId)
    {
        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() == ReservationStatus.BORROWED)
        {
            throw new RuntimeException("Borrowed reservations cannot be cancelled");
        }

        if (reservation.getStatus() == ReservationStatus.RETURNED)
        {
            throw new RuntimeException("Returned reservations cannot be cancelled");
        }

        if (reservation.getStatus() == ReservationStatus.EXPIRED)
        {
            throw new RuntimeException("Expired reservations cannot be cancelled");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED)
        {
            throw new RuntimeException("Reservation is already cancelled");
        }

        BookCopy bookCopy = reservation.getBookCopy();

        reservation.setStatus(ReservationStatus.CANCELLED);

        if (bookCopy != null &&
                (bookCopy.getStatus() == BookCopyStatus.RESERVED ||
                        bookCopy.getStatus() == BookCopyStatus.READY_FOR_PICKUP))
        {
            assignCopyToNextWaitingReservationOrMakeAvailable(reservation.getBook(), bookCopy);
        }

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation expireReservation(Long reservationId)
    {
        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() == ReservationStatus.BORROWED)
        {
            throw new RuntimeException("Borrowed reservations cannot be expired");
        }

        if (reservation.getStatus() == ReservationStatus.RETURNED)
        {
            throw new RuntimeException("Returned reservations cannot be expired");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED)
        {
            throw new RuntimeException("Cancelled reservations cannot be expired");
        }

        if (reservation.getStatus() == ReservationStatus.EXPIRED)
        {
            throw new RuntimeException("Reservation is already expired");
        }

        BookCopy bookCopy = reservation.getBookCopy();

        reservation.setStatus(ReservationStatus.EXPIRED);

        if (bookCopy != null)
        {
            assignCopyToNextWaitingReservationOrMakeAvailable(reservation.getBook(), bookCopy);
        }

        return reservationRepository.save(reservation);
    }

    @Transactional
    public void deleteReservation(Long reservationId)
    {
        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() == ReservationStatus.BORROWED)
        {
            throw new RuntimeException("Borrowed reservations cannot be deleted");
        }

        if (reservation.getStatus() == ReservationStatus.READY_FOR_PICKUP ||
                reservation.getStatus() == ReservationStatus.CONFIRMED)
        {
            throw new RuntimeException("Active reservations should be cancelled before deletion");
        }

        reservationRepository.delete(reservation);
    }

    private void assignCopyToNextWaitingReservationOrMakeAvailable(Book book, BookCopy bookCopy)
    {
        Reservation nextWaitingReservation = getNextWaitingReservation(book);

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
    }

    private void checkActiveReservationLimit(User user)
    {
        int activeReservations = 0;

        activeReservations += reservationRepository.findByUserAndStatus(user, ReservationStatus.CREATED).size();
        activeReservations += reservationRepository.findByUserAndStatus(user, ReservationStatus.CONFIRMED).size();
        activeReservations += reservationRepository.findByUserAndStatus(user, ReservationStatus.WAITING).size();
        activeReservations += reservationRepository.findByUserAndStatus(user, ReservationStatus.READY_FOR_PICKUP).size();
        activeReservations += reservationRepository.findByUserAndStatus(user, ReservationStatus.BORROWED).size();

        if (activeReservations >= MAX_ACTIVE_RESERVATIONS)
        {
            throw new RuntimeException("User has reached the maximum number of active reservations");
        }
    }

    private void checkDuplicateActiveReservation(User user, Book book)
    {
        List<Reservation> userReservations = reservationRepository.findByUser(user);

        for (Reservation reservation : userReservations)
        {
            boolean sameBook = reservation.getBook() != null &&
                    reservation.getBook().getBookId().equals(book.getBookId());

            boolean activeStatus = reservation.getStatus() == ReservationStatus.CREATED ||
                    reservation.getStatus() == ReservationStatus.CONFIRMED ||
                    reservation.getStatus() == ReservationStatus.WAITING ||
                    reservation.getStatus() == ReservationStatus.READY_FOR_PICKUP ||
                    reservation.getStatus() == ReservationStatus.BORROWED;

            if (sameBook && activeStatus)
            {
                throw new RuntimeException("User already has an active reservation for this book");
            }
        }
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

    private User getUserById(Long userId)
    {
        validateId(userId);

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    private Book getBookById(Long bookId)
    {
        validateId(bookId);

        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
    }

    private void validateReservationRequest(ReservationRequest request)
    {
        if (request == null)
        {
            throw new RuntimeException("Reservation request cannot be null");
        }

        if (request.getUserId() == null)
        {
            throw new RuntimeException("User id is required");
        }

        if (request.getBookId() == null)
        {
            throw new RuntimeException("Book id is required");
        }

        validateId(request.getUserId());
        validateId(request.getBookId());
    }

    private void validateId(Long id)
    {
        if (id == null || id <= 0)
        {
            throw new RuntimeException("Invalid id");
        }
    }

    private ReservationStatus parseReservationStatus(String statusText)
    {
        if (statusText == null || statusText.trim().isEmpty())
        {
            throw new RuntimeException("Reservation status is required");
        }

        try
        {
            return ReservationStatus.valueOf(statusText.trim().toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException("Invalid reservation status: " + statusText);
        }
    }
}