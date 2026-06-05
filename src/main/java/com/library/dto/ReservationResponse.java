package com.library.dto;

import com.library.model.ReservationStatus;
import java.time.LocalDateTime;

public class ReservationResponse
{
    private Long reservationId;
    private LocalDateTime reservationDate;
    private LocalDateTime expirationDate;
    private LocalDateTime pickupDate;
    private LocalDateTime returnDate;
    private ReservationStatus status;
    private BookResponse book;

    public ReservationResponse(Long reservationId, LocalDateTime reservationDate,
                               LocalDateTime expirationDate, LocalDateTime pickupDate,
                               LocalDateTime returnDate, ReservationStatus status,
                               BookResponse book)
    {
        this.reservationId = reservationId;
        this.reservationDate = reservationDate;
        this.expirationDate = expirationDate;
        this.pickupDate = pickupDate;
        this.returnDate = returnDate;
        this.status = status;
        this.book = book;
    }

    // getters (toate)
    public Long getReservationId() { return reservationId; }
    public LocalDateTime getReservationDate() { return reservationDate; }
    public LocalDateTime getExpirationDate() { return expirationDate; }
    public LocalDateTime getPickupDate() { return pickupDate; }
    public LocalDateTime getReturnDate() { return returnDate; }
    public ReservationStatus getStatus() { return status; }
    public BookResponse getBook() { return book; }
}