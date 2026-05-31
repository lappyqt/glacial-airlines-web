package com.lappyqt.glacialairlines.config.interseptor;

import com.lappyqt.glacialairlines.repositories.booking.BookingOrderRepository;
import com.lappyqt.glacialairlines.session.BookingSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class BookingExpiryInterceptor implements HandlerInterceptor {
    private final BookingSession bookingSession;
    private final BookingOrderRepository bookingOrderRepository;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        Long orderId = bookingSession.getOrderId();
        if (orderId == null) return true;

        boolean expired = bookingOrderRepository.findById(orderId)
                .map(order -> order.getBookingExpiresAt().isBefore(Instant.now()))
                .orElse(true);

        if (expired) {
            bookingSession.clear();
            response.sendRedirect("/?bookingOrderExpired=true");
            return false;
        }

        return true;
    }
}
