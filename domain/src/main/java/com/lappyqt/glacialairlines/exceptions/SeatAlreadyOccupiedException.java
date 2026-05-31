package com.lappyqt.glacialairlines.exceptions;

public class SeatAlreadyOccupiedException extends RuntimeException {
    public SeatAlreadyOccupiedException(String seatNumber) {
        super(String.format("Место %s уже занято", seatNumber));
    }
}
