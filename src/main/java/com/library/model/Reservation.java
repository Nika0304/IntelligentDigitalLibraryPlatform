package com.library.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    private LocalDateTime reservationDate;

    private LocalDateTime expirationDate;

    private LocalDateTime pickupDate;

    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.CREATED;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne
    @JoinColumn(name = "copy_id")
    private BookCopy bookCopy;

    public Reservation() {}

    public Reservation(User user, Book book)
    {
        this.user = user;
        this.book = book;
        this.reservationDate = LocalDateTime.now();
        this.status = ReservationStatus.CREATED;
    }

    public Long getReservationId()
    {
        return reservationId;
    }

    public void setReservationId(Long reservationId)
    {
        this.reservationId = reservationId;
    }

    public LocalDateTime getReservationDate()
    {
        return reservationDate;
    }

    public void setReservationDate(LocalDateTime reservationDate)
    {
        this.reservationDate = reservationDate;
    }

    public LocalDateTime getExpirationDate()
    {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate)
    {
        this.expirationDate = expirationDate;
    }

    public LocalDateTime getPickupDate()
    {
        return pickupDate;
    }

    public void setPickupDate(LocalDateTime pickupDate)
    {
        this.pickupDate = pickupDate;
    }

    public LocalDateTime getReturnDate()
    {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate)
    {
        this.returnDate = returnDate;
    }

    public ReservationStatus getStatus()
    {
        return status;
    }

    public void setStatus(ReservationStatus status)
    {
        this.status = status;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public Book getBook()
    {
        return book;
    }

    public void setBook(Book book)
    {
        this.book = book;
    }

    public BookCopy getBookCopy()
    {
        return bookCopy;
    }

    public void setBookCopy(BookCopy bookCopy)
    {
        this.bookCopy = bookCopy;
    }
}