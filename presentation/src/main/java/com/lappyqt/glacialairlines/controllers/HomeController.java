package com.lappyqt.glacialairlines.controllers;

import com.lappyqt.glacialairlines.services.FlightService;
import dto.SearchRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// Контроллер для обработки запросов к главной странице и странице программы лояльности
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {
    private final FlightService flightService;

    // Метод отображения главной страницы с формой поиска авиабилетов
    @GetMapping
    public String homePage(Model model) {
        // Инициализация DTO-объекта запроса поиска, если он отсутствует в модели
        if (!model.containsAttribute("searchRequestDto")) {
            model.addAttribute("searchRequestDto", new SearchRequestDto());
        }

        // Передача списка аэропортов в модель для заполнения выпадающих списков
        model.addAttribute("airports", flightService.getAirportList());
        return "index";
    }

    // Метод отображения информационной страницы о программе лояльности авиакомпании
    @GetMapping("/loyalty-program")
    public String loyaltyProgramPage() {
        return "loyalty-program";
    }
}
