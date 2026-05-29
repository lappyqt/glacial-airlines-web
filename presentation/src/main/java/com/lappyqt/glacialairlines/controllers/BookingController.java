package com.lappyqt.glacialairlines.controllers;

import com.lappyqt.glacialairlines.entities.account.UserAccount;
import com.lappyqt.glacialairlines.entities.booking.BookingOrder;
import com.lappyqt.glacialairlines.services.BookingService;
import com.lappyqt.glacialairlines.services.FlightService;
import com.lappyqt.glacialairlines.services.UserAccountService;
import com.lappyqt.glacialairlines.services.security.CustomUserDetails;
import com.lappyqt.glacialairlines.session.BookingSession;
import dto.PassengerDto;
import dto.PassengersFormDto;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {
    private final FlightService flightService;
    private final BookingService bookingService;
    private final UserAccountService userAccountService;
    private final BookingSession bookingSession;

    @GetMapping("/return-flight")
    public String returnFlightPage(Model model) {
        SearchRequestDto searchRequest = bookingSession.getSearchRequest();
        model.addAttribute("searchRequestDto", searchRequest);

        SearchResponseDto outboundFlight = flightService.getOutboundFlightOffer(bookingSession.getOutboundFlightId(), searchRequest);
        model.addAttribute("outboundFlight", outboundFlight);

        List<SearchResponseDto> returnFlights = flightService.findAvailableReturnFlightOffers(searchRequest);

        if (!returnFlights.isEmpty()) {
            BigDecimal minPrice = returnFlights.stream()
                    .map(SearchResponseDto::getTotalPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            model.addAttribute("minPrice", minPrice);
        }

        model.addAttribute("flightOffers", returnFlights);

        return "booking/return-flight";
    }

    @GetMapping("select-return")
    public String selectReturn(@RequestParam Long returnFlightId) {
        bookingSession.setReturnFlightId(returnFlightId);
        return "redirect:/booking/passengers";
    }

    @GetMapping("/passengers")
    public String passengersPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("searchRequestDto", bookingSession.getSearchRequest());

        UserAccount userAccount = userAccountService.findById(userDetails.getId());
        BookingOrder order = bookingService.getOrCreateDraft(
                bookingSession.getOrderId(),
                bookingSession.getOutboundFlightId(),
                bookingSession.getReturnFlightId(),
                bookingSession.getSearchRequest(),
                userAccount);
        bookingSession.setOrderId(order.getId());

        model.addAttribute("outboundFlight", flightService.getOutboundFlightOffer(
                bookingSession.getOutboundFlightId(), bookingSession.getSearchRequest()
        ));

        model.addAttribute("isRoundTrip", bookingSession.getReturnFlightId() != null);

        PassengersFormDto form = new PassengersFormDto();
        form.setContactEmail(order.getContactEmail());
        form.setContactPhone(order.getContactPhone());
        form.setPassengers(order.getPassengers().stream()
                .map(p -> {
                    PassengerDto dto = new PassengerDto();
                    dto.setFirstName(p.getFirstName());
                    dto.setLastName(p.getLastName());
                    dto.setMiddleName(p.getMiddleName());
                    dto.setGender(p.getGender());
                    dto.setBirthDate(p.getBirthDate());
                    dto.setDocumentType(p.getDocumentType());
                    dto.setDocumentNumber(p.getDocumentNumber());
                    dto.setPassengerType(p.getPassengerType());
                    return dto;
                }).collect(Collectors.toList()));

        model.addAttribute("passengersFormDto", form);
        return "booking/passengers";
    }

    @PostMapping("/passengers")
    public String savePassengers(@Valid @ModelAttribute PassengersFormDto form,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("searchRequestDto", bookingSession.getSearchRequest());
            model.addAttribute("outboundFlight", flightService.getOutboundFlightOffer(
                    bookingSession.getOutboundFlightId(), bookingSession.getSearchRequest()));
            model.addAttribute("isRoundTrip", bookingSession.getReturnFlightId() != null);

            return "booking/passengers";
        }

        bookingService.savePassengers(bookingSession.getOrderId(), form);
        return "redirect:/booking/services";
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
