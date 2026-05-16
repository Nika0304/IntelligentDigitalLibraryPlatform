package com.library.repository;

import com.library.model.Fine;
import com.library.model.FineStatus;
import com.library.model.Reservation;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FineRepository extends JpaRepository<Fine, Long>
{
    List<Fine> findByUser(User user);

    List<Fine> findByUserAndStatus(User user, FineStatus status);

    List<Fine> findByStatus(FineStatus status);

    Optional<Fine> findByReservation(Reservation reservation);

    boolean existsByReservation(Reservation reservation);
}