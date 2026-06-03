package com.lappyqt.glacialairlines.repositories.booking;

import com.lappyqt.glacialairlines.entities.booking.BookingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

// Репозиторий для работы с заказами авиабилетов в базе данных
public interface BookingOrderRepository extends JpaRepository<BookingOrder, Long> {
    // Поиск заказа со всеми данными рейсов, аэропортов и пассажиров
    @Query("""
        SELECT o FROM BookingOrder o
        JOIN FETCH o.outboundFlight of
        JOIN FETCH of.route r
        JOIN FETCH r.departureAirport
        JOIN FETCH r.arrivalAirport
        JOIN FETCH of.aircraft
        LEFT JOIN FETCH o.returnFlight rf
        LEFT JOIN FETCH rf.route rfr
        LEFT JOIN FETCH rfr.departureAirport
        LEFT JOIN FETCH rfr.arrivalAirport
        JOIN FETCH o.passengers p
        LEFT JOIN FETCH p.outboundSeatAvailability sa
        LEFT JOIN FETCH sa.seat
        JOIN FETCH o.userAccount ua
        JOIN FETCH ua.loyaltyAccount
        WHERE o.id = :id
    """)
    Optional<BookingOrder> findByIdWithPassengersAndFlights(@Param("id") Long id);

    // Поиск заказа вместе со списком выбранных дополнительных услуг
    @Query("""
        SELECT o FROM BookingOrder o
        LEFT JOIN FETCH o.selectedServices
        WHERE o.id = :id
    """)
    Optional<BookingOrder> findByIdWithServices(@Param("id") Long id);

    // Поиск не оплаченных вовремя заказов для их последующего аннулирования
    @Query("""
            SELECT o FROM BookingOrder o
            JOIN FETCH o.passengers p
            JOIN FETCH o.outboundFlight
            WHERE o.status IN ('DRAFT', 'PENDING_PAYMENT')
            AND o.bookingExpiresAt < :now
    """)
    List<BookingOrder> findExpiredOrders(@Param("now") Instant now);

    // Получение списка идентификаторов мест, выбранных в рамках заказа
    @Query("SELECT p.outboundSeatAvailability.id " +
            "FROM BookingOrder o " +
            "JOIN o.passengers p " +
            "WHERE o.id = :orderId AND p.outboundSeatAvailability IS NOT NULL")
    List<Long> findPassengerSeatIds(@Param("orderId") Long orderId);

    // Проверка, добавлена ли к заказу услуга возвратного тарифа
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM BookingOrder o " +
            "JOIN o.selectedServices s " +
            "WHERE o.id = :orderId AND s.serviceType = 'REFUND'")
    boolean hasRefundService(@Param("orderId") Long orderId);

    // Получение истории успешных заказов конкретного пользователя для личного кабинета
    @Query("""
        SELECT o FROM BookingOrder o
        JOIN FETCH o.outboundFlight of
        JOIN FETCH of.route r
        JOIN FETCH r.departureAirport
        JOIN FETCH r.arrivalAirport
        JOIN FETCH of.aircraft
        LEFT JOIN FETCH o.returnFlight rf
        LEFT JOIN FETCH rf.route rfr
        LEFT JOIN FETCH rfr.departureAirport
        LEFT JOIN FETCH rfr.arrivalAirport
        JOIN FETCH o.passengers p
        LEFT JOIN FETCH p.outboundSeatAvailability sa
        LEFT JOIN FETCH sa.seat
        WHERE o.userAccount.id = :userId
        AND o.status IN ('PAID', 'COMPLETED')
        ORDER BY o.createdAt DESC
    """)
    List<BookingOrder> findOrdersByUserId(@Param("userId") Long userId);

    // Загрузка дополнительных услуг для списка заказов при пакетной обработке
    @Query("""
        SELECT o FROM BookingOrder o
        LEFT JOIN FETCH o.selectedServices
        WHERE o.id IN :ids
    """)
    List<BookingOrder> findOrdersWithServices(@Param("ids") List<Long> ids);
}
