package com.lappyqt.glacialairlines.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/booking")
public class BookingController {
    @GetMapping("/return-flight")
    public String returnFlightPage() {
        return "booking/return-flight";
    }

    @GetMapping("/passengers")
    public String passengersPage() {
        return "booking/passengers";
    }

    @GetMapping("/services")
    public String servicesPage() {
        return "booking/services";
    }

    @GetMapping("/checkout")
    public String checkoutPage() {
        return "booking/checkout";
    }

    @GetMapping("/success")
    public String successPage() {
        return "booking/success";
    }
}
