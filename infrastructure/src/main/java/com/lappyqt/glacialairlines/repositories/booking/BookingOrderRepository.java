package com.lappyqt.glacialairlines.repositories.booking;

import com.lappyqt.glacialairlines.entities.booking.BookingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingOrderRepository extends JpaRepository<BookingOrder, Long> {
    @Query("""
        SELECT o FROM BookingOrder o
        LEFT JOIN FETCH o.passengers
        WHERE o.id = :id
    """)
    Optional<BookingOrder> findByIdWithPassengers(@Param("id") Long id);

    @Query("""
        SELECT o FROM BookingOrder o
        JOIN FETCH o.outboundFlight of
        JOIN FETCH of.route r
        JOIN FETCH r.departureAirport
        JOIN FETCH r.arrivalAirport
        JOIN FETCH of.aircraft
        LEFT JOIN FETCH o.returnFlight rf
        LEFT JOIN FETCH o.passengers p
        LEFT JOIN FETCH p.outboundSeatAvailability sa
        LEFT JOIN FETCH sa.seat
        WHERE o.id = :id
    """)
    Optional<BookingOrder> findByIdWithPassengersAndFlights(@Param("id") Long id);

    @Query("""
        SELECT o FROM BookingOrder o
        LEFT JOIN FETCH o.passengers p
        LEFT JOIN FETCH p.outboundSeatAvailability sa
        LEFT JOIN FETCH sa.seat
        JOIN FETCH o.userAccount ua
        JOIN FETCH ua.loyaltyAccount
        WHERE o.id = :id
    """)
    Optional<BookingOrder> findByIdWithPassengersAndAccount(@Param("id") Long id);

    @Query("""
            SELECT o FROM BookingOrder o
            JOIN FETCH o.passengers p
            JOIN FETCH o.outboundFlight
            WHERE o.status IN ('DRAFT', 'PENDING_PAYMENT')
            AND o.bookingExpiresAt < :now
    """)
    List<BookingOrder> findExpiredOrders(@Param("now") Instant now);

    @Query("""
        SELECT o FROM BookingOrder o
        LEFT JOIN FETCH o.selectedServices
        WHERE o.id = :id
    """)
    Optional<BookingOrder> findByIdWithServices(@Param("id") Long id);
}
