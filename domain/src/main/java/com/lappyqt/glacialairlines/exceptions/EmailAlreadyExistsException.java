package com.lappyqt.glacialairlines.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("Email уже занят: " + email);
    }
}
