package com.lappyqt.glacialairlines.repositories.flight;

import com.lappyqt.glacialairlines.entities.flight.SeatAvailability;
import com.lappyqt.glacialairlines.enums.SeatClass;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// Репозиторий для работы со схемой мест самолета в базе данных
public interface SeatAvailabilityRepository extends JpaRepository<SeatAvailability, Long> {

    // Получение всех мест рейса с сортировкой по рядам и буквам для отрисовки карты мест
    @Query("""
        SELECT sa FROM SeatAvailability sa
        JOIN FETCH sa.seat s
        WHERE sa.flight.id = :flightId
        ORDER BY s.rowNumber, s.seatLetter
    """)
    List<SeatAvailability> findByFlightIdAndSeatClass(@Param("flightId") Long flightId);

    // Получение списка мест по их идентификаторам вместе с данными кресел
    @Query("""
        SELECT sa FROM SeatAvailability sa
        JOIN FETCH sa.seat
        WHERE sa.id IN :ids
    """)
    List<SeatAvailability> findByIdsWithSeat(@Param("ids") List<Long> ids);

    // Блокировка выбранных мест в БД (Pessimistic Write) для предотвращения их одновременной покупки
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT sa FROM SeatAvailability sa
    WHERE sa.id IN :ids
    """)
    List<SeatAvailability> findByIdsWithLock(@Param("ids") List<Long> ids);
}
