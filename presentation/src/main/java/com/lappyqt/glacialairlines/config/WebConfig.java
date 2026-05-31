package com.lappyqt.glacialairlines.config;

import com.lappyqt.glacialairlines.config.interseptor.BookingExpiryInterceptor;
import com.lappyqt.glacialairlines.config.interseptor.BookingSessionInterceptor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final BookingSessionInterceptor bookingSessionInterceptor;
    private final BookingExpiryInterceptor bookingExpiryInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(bookingSessionInterceptor)
                .addPathPatterns("/booking/**")
                .excludePathPatterns("/booking/success");

        interceptorRegistry.addInterceptor(bookingExpiryInterceptor)
                .addPathPatterns("/booking/**")
                .excludePathPatterns("/booking/success");
    }
}
