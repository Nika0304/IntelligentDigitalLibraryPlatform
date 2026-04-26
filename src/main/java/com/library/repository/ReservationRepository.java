package com.library.repository;

import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import com.library.model.Book;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
    List<Reservation> findByBook(Book book);
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByUserAndStatus(User user, ReservationStatus status);
    List<Reservation> findByBookAndStatusOrderByReservationDateAsc(Book book, ReservationStatus status);
}
