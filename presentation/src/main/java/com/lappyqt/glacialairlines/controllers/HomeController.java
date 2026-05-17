package com.lappyqt.glacialairlines.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {
    @GetMapping("/loyalty-program")
    public String loyaltyProgramPage() {
        return "loyalty-program";
    }
}
