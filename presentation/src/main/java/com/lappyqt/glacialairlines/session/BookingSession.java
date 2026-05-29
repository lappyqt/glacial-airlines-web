package com.lappyqt.glacialairlines.session;

import dto.SearchRequestDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Getter
@Setter
public class BookingSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long outboundFlightId;

    private Long returnFlightId;

    private SearchRequestDto searchRequest;

    private Long orderId;
}
