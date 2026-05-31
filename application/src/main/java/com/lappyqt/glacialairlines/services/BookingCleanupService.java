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

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingCleanupService {
    private final BookingOrderRepository bookingOrderRepository;
    private final FlightInventoryRepository flightInventoryRepository;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cleanupExpiredOrders() {
        List<BookingOrder> expired = bookingOrderRepository.findExpiredOrders(Instant.now());

        for (BookingOrder order: expired) {
            if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                flightInventoryRepository.findByFlightIdAndSeatClass(
                        order.getOutboundFlight().getId(), order.getSeatClass())
                        .ifPresent(inv -> {
                            inv.setAvailableSeats(inv.getAvailableSeats() + order.getPassengers().size());
                            flightInventoryRepository.save(inv);
                        });
            }

            bookingOrderRepository.delete(order);
        }

        if (!expired.isEmpty()) {
            log.info("Удалено неактивных черновиков заказов: {}", expired.size());
        }
    }
}
