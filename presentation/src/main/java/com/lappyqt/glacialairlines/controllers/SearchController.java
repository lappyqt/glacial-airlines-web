package com.lappyqt.glacialairlines.controllers;

import com.lappyqt.glacialairlines.entities.flight.Airport;
import com.lappyqt.glacialairlines.exceptions.OutboundDateAfterReturnDateException;
import com.lappyqt.glacialairlines.exceptions.PassengerLimitExceededException;
import com.lappyqt.glacialairlines.services.FlightService;
import com.lappyqt.glacialairlines.services.security.CustomUserDetails;
import com.lappyqt.glacialairlines.session.BookingSession;
import dto.SearchRequestDto;
import dto.SearchResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

// Контроллер для обработки поисковых запросов авиабилетов и первичного выбора рейсов "туда"
@Controller
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
    private final FlightService flightService;
    private final BookingSession bookingSession;

    // Метод обработки формы поиска рейсов с поддержкой валидации, фильтрации и сортировки результатов
    @GetMapping
    public String searchPage(@Valid @ModelAttribute("searchRequestDto") SearchRequestDto searchRequestDto,
                             BindingResult bindingResult,
                             @RequestParam(value = "source", required = false) String source,
                             @RequestParam(value = "filter", required = false, defaultValue = "price") String filter,
                             Model model,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Airport> airports = flightService.getAirportList();
        model.addAttribute("airports", airports);
        model.addAttribute("filter", filter);

        // Проверка бизнес-правила: аэропорты отправления и назначения не должны совпадать
        if (searchRequestDto.getReturnAirportId() != null &&
                searchRequestDto.getOutboundAirportId() != null &&
                searchRequestDto.getOutboundAirportId().equals(searchRequestDto.getReturnAirportId())) {
            bindingResult.rejectValue("returnAirportId", "returnAirportId.same",
                    "Аэропорт вылета и прилёта не могут совпадать");
        }

        // Возврат на исходную страницу при обнаружении ошибок валидации полей формы
        if (bindingResult.hasErrors()) return "index".equals(source) ? "index" : "search";

        // Получение текущего баланса миль авторизованного пользователя для расчетов стоимости
        Integer milesObject = (Integer) model.getAttribute("currentMiles");
        int currentMiles = (milesObject != null) ? milesObject : 0;

        try {
            // Получаем доступные предложения для перелета на конкретную дату
            List<SearchResponseDto> flightOffers = flightService.findAvailableFlightOffers(searchRequestDto, currentMiles, filter);
            model.addAttribute("flightOffers", flightOffers);

            // Сбор статистических данных (минимальная цена, города) для отображения в шапке результатов поиска
            if (!flightOffers.isEmpty()) {
                BigDecimal minPrice = flightOffers.stream()
                        .map(SearchResponseDto::getTotalPrice)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

                model.addAttribute("minPrice", minPrice);
                model.addAttribute("departureCity", flightOffers.getFirst().getDepartureCity());
                model.addAttribute("arrivalCity", flightOffers.getFirst().getArrivalCity());
            }
        }

        // Обработка исключений некорректно введенного периода дат
        catch (OutboundDateAfterReturnDateException exception) {
            bindingResult.reject("outboundDateAfterReturnDate", exception.getMessage());
            return "index".equals(source) ? "index" : "search";
        }

        // Обработка исключений превышения лимита на одновременную покупку билетов
        catch (PassengerLimitExceededException exception) {
            bindingResult.reject("passengerLimitExceeded", exception.getMessage());
            return "index".equals(source) ? "index" : "search";
        }

        // Сохранение параметров успешного поиска в сессию пользователя
        bookingSession.setSearchRequest(searchRequestDto);
        return "search";
    }

    // Метод выбора конкретного рейса "туда" и "редирект" на следующий этап бронирования
    @GetMapping("select-outbound")
    public String selectOutbound(@RequestParam Long outboundFlightId) {
        // Фиксация выбранного рейса и расчет стоимости предложения в сессии бронирования
        bookingSession.setOutboundFlightId(outboundFlightId);

        SearchRequestDto searchRequest = bookingSession.getSearchRequest();

        // Определение типа перелета (в одну сторону или туда-обратно)
        boolean isRoundTrip = (searchRequest.getReturnAirportId() != null
                && searchRequest.getReturnFlightDate() != null);

        SearchResponseDto outboundFlight = flightService.getFlightOffer(outboundFlightId, searchRequest);
        bookingSession.setOutboundOfferPrice(outboundFlight.getTotalPrice());

        // Перенаправление на выбор обратного билета или на форму ввода данных пассажиров
        return isRoundTrip
                ? "redirect:/booking/return-flight"
                : "redirect:/booking/passengers";
    }
}
