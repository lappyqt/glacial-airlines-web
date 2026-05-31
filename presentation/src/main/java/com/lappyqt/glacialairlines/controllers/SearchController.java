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

@Controller
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
    private final FlightService flightService;
    private final BookingSession bookingSession;

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

        if (searchRequestDto.getReturnAirportId() != null &&
                searchRequestDto.getOutboundAirportId() != null &&
                searchRequestDto.getOutboundAirportId().equals(searchRequestDto.getReturnAirportId())) {
            bindingResult.rejectValue("returnAirportId", "returnAirportId.same",
                    "Аэропорт вылета и прилёта не могут совпадать");
        }

        if (bindingResult.hasErrors()) return "index".equals(source) ? "index" : "search";

        Integer milesObject = (Integer) model.getAttribute("currentMiles");
        int currentMiles = (milesObject != null) ? milesObject : 0;

        try {
            List<SearchResponseDto> flightOffers = flightService.findAvailableFlightOffers(searchRequestDto, currentMiles, filter);
            model.addAttribute("flightOffers", flightOffers);

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
        catch (OutboundDateAfterReturnDateException exception) {
            bindingResult.reject("outboundDateAfterReturnDate", exception.getMessage());
            return "index".equals(source) ? "index" : "search";
        }
        catch (PassengerLimitExceededException exception) {
            bindingResult.reject("passengerLimitExceeded", exception.getMessage());
            return "index".equals(source) ? "index" : "search";
        }

        bookingSession.setSearchRequest(searchRequestDto);
        return "search";
    }

    @GetMapping("select-outbound")
    public String selectOutbound(@RequestParam Long outboundFlightId) {
        bookingSession.setOutboundFlightId(outboundFlightId);

        SearchRequestDto searchRequest = bookingSession.getSearchRequest();
        boolean isRoundTrip = (searchRequest.getReturnAirportId() != null
                && searchRequest.getReturnFlightDate() != null);

        SearchResponseDto outboundFlight = flightService.getFlightOffer(outboundFlightId, searchRequest);
        bookingSession.setOutboundOfferPrice(outboundFlight.getTotalPrice());

        return isRoundTrip
                ? "redirect:/booking/return-flight"
                : "redirect:/booking/passengers";
    }
}
