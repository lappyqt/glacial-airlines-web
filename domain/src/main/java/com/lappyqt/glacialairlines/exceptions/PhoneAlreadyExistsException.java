package com.lappyqt.glacialairlines.exceptions;

public class PhoneAlreadyExistsException extends RuntimeException {
    public PhoneAlreadyExistsException(String phoneNumber) {
        super("Номер телефона уже занят: " + phoneNumber);
    }
}
