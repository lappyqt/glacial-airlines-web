package com.lappyqt.glacialairlines.enums;

public enum SeatClass {
    ECONOMY(2.5),
    EMERGENCY(2.5),
    BUSINESS(8.5);

    private final double milesPercent;

    SeatClass(double milesPercent) {
        this.milesPercent = milesPercent;
    }

    public double getMilesPercent() {
        return milesPercent;
    }
}