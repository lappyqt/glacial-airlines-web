package com.lappyqt.glacialairlines.repositories.flight;

import com.lappyqt.glacialairlines.entities.flight.FlightInventory;
import com.lappyqt.glacialairlines.enums.FlightStatus;
import com.lappyqt.glacialairlines.enums.SeatClass;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Репозиторий для контроля доступных тарифов и количества свободных мест на рейсах
public interface FlightInventoryRepository extends JpaRepository<FlightInventory, Long> {
    // Поиск доступных рейсов по городам, дате, классу обслуживания и числу пассажиров
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

    // Получение информации о наличии свободных мест конкретного класса на рейсе
    @Query("""
        SELECT fi FROM FlightInventory fi
        JOIN FETCH fi.flight f
        JOIN FETCH f.aircraft
        JOIN FETCH f.route r
        JOIN FETCH r.departureAirport
        JOIN FETCH r.arrivalAirport
        WHERE f.id = :flightId
          AND fi.seatClass = :seatClass
    """)
    Optional<FlightInventory> findByFlightIdAndSeatClass(
            @Param("flightId") Long flightId,
            @Param("seatClass") SeatClass seatClass
    );

    // Блокировка счетчика мест в БД (Pessimistic Write) на время списания доступных билетов при покупке
    @Query("""
        SELECT fi FROM FlightInventory fi
        JOIN FETCH fi.flight f
        JOIN FETCH f.aircraft
        JOIN FETCH f.route r
        JOIN FETCH r.departureAirport
        JOIN FETCH r.arrivalAirport
        WHERE f.id = :flightId
          AND fi.seatClass = :seatClass
    """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FlightInventory> findByFlightIdAndSeatClassWithLock(
            @Param("flightId") Long flightId,
            @Param("seatClass") SeatClass seatClass
    );
}
