package com.lappyqt.glacialairlines.repositories.flight;

import com.lappyqt.glacialairlines.entities.flight.Flight;
import com.lappyqt.glacialairlines.entities.flight.FlightInventory;
import com.lappyqt.glacialairlines.enums.FlightStatus;
import com.lappyqt.glacialairlines.enums.SeatClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FlightInventoryRepository extends JpaRepository<Flight, Long> {
    @Query("""
        SELECT fi FROM FlightInventory fi
        JOIN FETCH fi.flight f
        JOIN FETCH f.aircraft
        JOIN FETCH f.route r
        JOIN FETCH r.departureAirport
        JOIN FETCH r.arrivalAirport
        WHERE r.departureAirport.id = :departureAirportId
          AND r.arrivalAirport.id   = :arrivalAirportId
          AND fi.seatClass          = :seatClass
          AND fi.availableSeats     >= :totalPassengers
          AND f.status              = :status
          AND CAST(f.departureTime AS DATE) = :date
        """)
    List<FlightInventory> findAvailable(
            @Param("departureAirportId") Long departureAirportId,
            @Param("arrivalAirportId")   Long arrivalAirportId,
            @Param("seatClass")          SeatClass seatClass,
            @Param("totalPassengers")    int totalPassengers,
            @Param("status")             FlightStatus status,
            @Param("date")               LocalDate date
    );
}
