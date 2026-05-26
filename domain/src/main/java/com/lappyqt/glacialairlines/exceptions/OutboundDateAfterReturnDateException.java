package com.lappyqt.glacialairlines.exceptions;

import java.time.LocalDate;

public class OutboundDateAfterReturnDateException extends RuntimeException {
    public OutboundDateAfterReturnDateException(LocalDate departureDate, LocalDate returnDate) {
        super(String.format("Дата вылета %s идет после даты возвращения %s", departureDate.toString(), returnDate.toString()));
    }
}
