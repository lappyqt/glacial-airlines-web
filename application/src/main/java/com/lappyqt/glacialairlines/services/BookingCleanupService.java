package com.lappyqt.glacialairlines.services;

import com.lappyqt.glacialairlines.entities.booking.BookingOrder;
import com.lappyqt.glacialairlines.enums.OrderStatus;
import com.lappyqt.glacialairlines.repositories.booking.BookingOrderRepository;
import com.lappyqt.glacialairlines.repositories.flight.FlightInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

// Сервисный класс фоновой очистки просроченных черновиков заказов и возврата мест в инвентарь рейсов
@Service
@Slf4j
@RequiredArgsConstructor
public class BookingCleanupService {
    private final BookingOrderRepository bookingOrderRepository;
    private final FlightInventoryRepository flightInventoryRepository;

    // Метод фонового планировщика для регулярного удаления истекших бронирований (запуск раз в 60 секунд)
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cleanupExpiredOrders() {
        // Поиск в репозитории всех заказов, у которых время брони меньше текущего момента времени
        List<BookingOrder> expired = bookingOrderRepository.findExpiredOrders(Instant.now());

        // Итерация по списку просроченных заказов для их аннулирования
        for (BookingOrder order: expired) {
            // Если заказ дошел до этапа ожидания оплаты, значит места на рейс уже были заблокированы в инвентаре
            if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                // Поиск инвентаря для конкретного вылетающего рейса и класса обслуживания
                flightInventoryRepository.findByFlightIdAndSeatClass(
                        order.getOutboundFlight().getId(), order.getSeatClass())
                        .ifPresent(inv -> {
                            // Возврат забронированных мест обратно в продажу
                            inv.setAvailableSeats(inv.getAvailableSeats() + order.getPassengers().size());
                            flightInventoryRepository.save(inv);
                        });
            }

            // Физическое удаление записи просроченного черновика
            bookingOrderRepository.delete(order);
        }

        if (!expired.isEmpty()) {
            log.info("Удалено неактивных черновиков заказов: {}", expired.size());
        }
    }
}
