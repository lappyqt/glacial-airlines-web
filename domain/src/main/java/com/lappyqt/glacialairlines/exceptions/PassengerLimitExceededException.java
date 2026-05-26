package com.lappyqt.glacialairlines.exceptions;

public class PassengerLimitExceededException extends RuntimeException {
    public PassengerLimitExceededException(Integer passengerCount) {
        super("Превышено количество пассажиров для оформления заказа: " + passengerCount);
    }
}
