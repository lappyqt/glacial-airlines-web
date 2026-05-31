package com.lappyqt.glacialairlines.controllers;

import com.lappyqt.glacialairlines.entities.account.UserAccount;
import com.lappyqt.glacialairlines.entities.booking.AdditionalService;
import com.lappyqt.glacialairlines.entities.booking.BookingOrder;
import com.lappyqt.glacialairlines.services.BookingService;
import com.lappyqt.glacialairlines.services.FlightService;
import com.lappyqt.glacialairlines.services.UserAccountService;
import com.lappyqt.glacialairlines.services.security.CustomUserDetails;
import com.lappyqt.glacialairlines.session.BookingSession;
import dto.*;
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
    public String returnFlightPage(Model model, @RequestParam(value = "filter", required = false, defaultValue = "price") String filter) {
        SearchRequestDto searchRequest = bookingSession.getSearchRequest();
        model.addAttribute("searchRequestDto", searchRequest);
        model.addAttribute("filter", filter);

        SearchResponseDto outboundFlight = flightService.getFlightOffer(bookingSession.getOutboundFlightId(), searchRequest);
        model.addAttribute("outboundFlight", outboundFlight);

        Integer milesObject = (Integer) model.getAttribute("currentMiles");
        int currentMiles = (milesObject != null) ? milesObject : 0;

        List<SearchResponseDto> returnFlights = flightService.findAvailableReturnFlightOffers(searchRequest, currentMiles, filter);

        if (!returnFlights.isEmpty()) {
            BigDecimal minPrice = returnFlights.stream()
                    .map(SearchResponseDto::getTotalPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            model.addAttribute("departureCity", outboundFlight.getDepartureCity());
            model.addAttribute("arrivalCity", outboundFlight.getArrivalCity());
            model.addAttribute("minPrice", minPrice);
        }

        model.addAttribute("flightOffers", returnFlights);

        return "booking/return-flight";
    }

    @GetMapping("select-return")
    public String selectReturn(@RequestParam Long returnFlightId) {
        bookingSession.setReturnFlightId(returnFlightId);
        bookingSession.setReturnOfferPrice(flightService.getFlightOffer(returnFlightId, bookingSession.getSearchRequest()).getTotalPrice());

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

        model.addAttribute("outboundFlight", flightService.getFlightOffer(
                bookingSession.getOutboundFlightId(), bookingSession.getSearchRequest()
        ));

        BigDecimal returnFlightPrice = bookingSession.getReturnOfferPrice();
        model.addAttribute("basePrice", bookingSession.getOutboundOfferPrice().add(
                returnFlightPrice != null ? returnFlightPrice : BigDecimal.ZERO
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
            model.addAttribute("outboundFlight", flightService.getFlightOffer(
                    bookingSession.getOutboundFlightId(), bookingSession.getSearchRequest()));
            model.addAttribute("isRoundTrip", bookingSession.getReturnFlightId() != null);

            BigDecimal returnFlightPrice = bookingSession.getReturnOfferPrice();
            model.addAttribute("basePrice", bookingSession.getOutboundOfferPrice().add(
                    returnFlightPrice != null ? returnFlightPrice : BigDecimal.ZERO
            ));

            return "booking/passengers";
        }

        BigDecimal returnFlightPrice = bookingSession.getReturnOfferPrice();
        BigDecimal basePrice = bookingSession.getOutboundOfferPrice().add(
                returnFlightPrice != null ? returnFlightPrice : BigDecimal.ZERO
        );

        bookingService.savePassengersAndSetBasePrice(bookingSession.getOrderId(), basePrice, form);
        return "redirect:/booking/services";
    }

    @GetMapping("/services")
    public String servicesPage(Model model) {
        BookingOrder bookingOrder = bookingService.getOrder(bookingSession.getOrderId());
        List<SeatGroupDto> seatGroups = flightService.getSeatGroups(bookingOrder.getOutboundFlight().getId());
        List<AdditionalService> additionalServices = bookingService.getAdditionalServices();

        List<Long> selectedServiceIds = bookingOrder.getSelectedServices().stream()
                .map(AdditionalService::getId)
                .toList();

        ServicesFormDto form = new ServicesFormDto();
        form.setSelectedServiceIds(selectedServiceIds);

        model.addAttribute("seatClassName", SeatGroupDto.getGroupName(bookingOrder.getSeatClass()));
        model.addAttribute("seatGroups", seatGroups);
        model.addAttribute("bookingOrder", bookingOrder);
        model.addAttribute("availableServices", additionalServices);
        model.addAttribute("servicesForm", form);

        return "booking/services";
    }

    @PostMapping("/services")
    public String saveSelectedServices(@ModelAttribute ServicesFormDto servicesFormDto) {
        PrepareCheckoutResponseDto prepareCheckoutResponseDto = bookingService.saveServicesAndPrepareCheckout(bookingSession.getOrderId(), servicesFormDto);
        bookingSession.setSeatsSurcharge(prepareCheckoutResponseDto.getSeatsSurcharge());
        bookingSession.setServicesTotal(prepareCheckoutResponseDto.getServicesTotal());

        return "redirect:/booking/checkout";
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model) {
        BookingOrder order = bookingService.getFullOrder(bookingSession.getOrderId());
        SearchRequestDto searchRequest = bookingSession.getSearchRequest();

        model.addAttribute("bookingOrder", order);
        model.addAttribute("outboundFlight", flightService.getFlightOffer(
                order.getOutboundFlight().getId(), searchRequest));

        if (order.getReturnFlight() != null) {
            model.addAttribute("returnFlight", flightService.getFlightOffer(
                    order.getReturnFlight().getId(), searchRequest));
        }

        model.addAttribute("paymentFormDto", new PaymentFormDto());
        model.addAttribute("seatsSurcharge", bookingSession.getSeatsSurcharge());
        model.addAttribute("servicesTotal", bookingSession.getServicesTotal());

        return "booking/checkout";
    }

    @PostMapping("/checkout")
    public String processPayment(@Valid @ModelAttribute PaymentFormDto paymentFormDto,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            BookingOrder order = bookingService.getFullOrder(bookingSession.getOrderId());
            SearchRequestDto searchRequest = bookingSession.getSearchRequest();

            model.addAttribute("bookingOrder", order);
            model.addAttribute("outboundFlight", flightService.getFlightOffer(
                    order.getOutboundFlight().getId(), searchRequest));

            if (order.getReturnFlight() != null) {
                model.addAttribute("returnFlight", flightService.getFlightOffer(
                        order.getReturnFlight().getId(), searchRequest));
            }

            model.addAttribute("seatsSurcharge", bookingSession.getSeatsSurcharge());
            model.addAttribute("servicesTotal", bookingSession.getServicesTotal());

            return "booking/checkout";
        }

        bookingService.processPayment(bookingSession.getOrderId(), paymentFormDto);
        bookingSession.clear();

        return "redirect:/booking/success";
    }

    @GetMapping("/success")
    public String successPage() {
        return "booking/success";
    }
}
