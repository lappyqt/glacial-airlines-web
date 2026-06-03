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

// Класс-компонент, которые проверяет актуальность заказа-черновика
@Component
@RequiredArgsConstructor
public class BookingExpiryInterceptor implements HandlerInterceptor {
    private final BookingSession bookingSession;
    private final BookingOrderRepository bookingOrderRepository;

    // Метод пре-обработки запроса. Пропускает запрос, если время бронирования не истекло и возвращает на главную, если черновик заказа уже удалён.
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // Получаем ID активного черновика
        Long orderId = bookingSession.getOrderId();
        if (orderId == null) return true;

        // Проверяем: не истекло ли время черновика
        boolean expired = bookingOrderRepository.findById(orderId)
                .map(order -> order.getBookingExpiresAt().isBefore(Instant.now()))
                .orElse(true);

        // Очищаем сессию и возвращаем на главную, в случае удаления черновика
        if (expired) {
            bookingSession.clear();
            response.sendRedirect("/?bookingOrderExpired=true");
            return false;
        }

        return true;
    }
}
