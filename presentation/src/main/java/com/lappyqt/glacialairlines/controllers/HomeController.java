package com.lappyqt.glacialairlines.controllers;

import com.lappyqt.glacialairlines.services.FlightService;
import dto.SearchRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {
    private final FlightService flightService;

    @GetMapping
    public String homePage(Model model) {
        if (!model.containsAttribute("searchRequestDto")) {
            model.addAttribute("searchRequestDto", new SearchRequestDto());
        }

        model.addAttribute("airports", flightService.getAirportList());
        return "index";
    }

    @GetMapping("/loyalty-program")
    public String loyaltyProgramPage() {
        return "loyalty-program";
    }
}
