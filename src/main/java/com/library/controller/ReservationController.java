package com.library.controller;

import com.library.dto.ReservationRequest;
import com.library.model.Reservation;
import com.library.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController
{

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService)
    {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations()
    {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<?> getReservationById(@PathVariable Long reservationId)
    {
        try
        {
            return ResponseEntity.ok(reservationService.getReservationById(reservationId));
        }
        catch (RuntimeException e)
        {
            return handleReservationException(e);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getReservationsByUserId(@PathVariable Long userId)
    {
        try
        {
            return ResponseEntity.ok(reservationService.getReservationsByUserId(userId));
        }
        catch (RuntimeException e)
        {
            return handleReservationException(e);
        }
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<?> getReservationsByBookId(@PathVariable Long bookId)
    {
        try
        {
            return ResponseEntity.ok(reservationService.getReservationsByBookId(bookId));
        }
        catch (RuntimeException e)
        {
            return handleReservationException(e);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getReservationsByStatus(@PathVariable String status)
    {
        try
        {
            return ResponseEntity.ok(reservationService.getReservationsByStatus(status));
        }
        catch (RuntimeException e)
        {
            return handleReservationException(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody ReservationRequest request)
    {
        try
        {
            return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(request));
        }
        catch (RuntimeException e)
        {
            return handleReservationException(e);
        }
    }

    @PutMapping("/{reservationId}/ready-for-pickup")
    public ResponseEntity<?> markReadyForPickup(@PathVariable Long reservationId)
    {
        try
        {
            return ResponseEntity.ok(reservationService.markReadyForPickup(reservationId));
        }
        catch (RuntimeException e)
        {
            return handleReservationException(e);
        }
    }

    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId)
    {
        try
        {
            return ResponseEntity.ok(reservationService.cancelReservation(reservationId));
        }
        catch (RuntimeException e)
        {
            return handleReservationException(e);
        }
    }

    @PutMapping("/{reservationId}/expire")
    public ResponseEntity<?> expireReservation(@PathVariable Long reservationId)
    {
        try
        {
            return ResponseEntity.ok(reservationService.expireReservation(reservationId));
        }
        catch (RuntimeException e)
        {
            return handleReservationException(e);
        }
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<?> deleteReservation(@PathVariable Long reservationId)
    {
        try
        {
            reservationService.deleteReservation(reservationId);
            return ResponseEntity.noContent().build();
        }
        catch (RuntimeException e)
        {
            return handleReservationException(e);
        }
    }

    private ResponseEntity<String> handleReservationException(RuntimeException e)
    {
        String message = e.getMessage();

        if (message != null && message.toLowerCase().contains("not found"))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }

        if (message != null && (
                message.toLowerCase().contains("already") ||
                        message.toLowerCase().contains("cannot") ||
                        message.toLowerCase().contains("only") ||
                        message.toLowerCase().contains("maximum") ||
                        message.toLowerCase().contains("no physical copies") ||
                        message.toLowerCase().contains("active reservation")
        ))
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}