package com.lappyqt.glacialairlines.config.interseptor;


import com.lappyqt.glacialairlines.session.BookingSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class BookingSessionInterceptor implements HandlerInterceptor {
    private final BookingSession bookingSession;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (bookingSession.getSearchRequest() == null) {
            response.sendRedirect("/?sessionExpired=true");
            return false;
        }

        return true;
    }
}
