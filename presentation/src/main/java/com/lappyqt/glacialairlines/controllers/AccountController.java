package com.lappyqt.glacialairlines.controllers;

import com.lappyqt.glacialairlines.entities.account.Passenger;
import com.lappyqt.glacialairlines.entities.account.UserAccount;
import com.lappyqt.glacialairlines.services.BookingService;
import com.lappyqt.glacialairlines.services.UserAccountService;
import com.lappyqt.glacialairlines.services.security.CustomUserDetails;
import dto.BookingOrderDto;
import dto.CreatePassengerDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final UserAccountService userAccountService;
    private final BookingService bookingService;

    @GetMapping
    public String accountPageDefault(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        return accountPage("orders", userDetails, model);
    }

    @GetMapping(value = {"/", "/{tab}"})
    public String accountPage(@PathVariable(required = false, name = "tab")
                              String tab,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        String activeTab = (tab != null && !tab.isEmpty()) ? tab : "orders";
        model.addAttribute("activeTab", activeTab);

        UserAccount userAccount = userAccountService.findByIdWithTransactions(userDetails.getId());
        model.addAttribute("userAccount", userAccount);

        if (activeTab.equalsIgnoreCase("orders")) {
            List<BookingOrderDto> bookingOrders = userAccountService.getUserOrders(userDetails.getId());
            model.addAttribute("bookingOrders", bookingOrders);
        }

        if (activeTab.equalsIgnoreCase("documents")) {
            CreatePassengerDto createPassengerDto = getCreatePassengerDto(userAccount);

            model.addAttribute("createPassengerDto", createPassengerDto);
        }

        return "account/main";
    }

    private @NonNull CreatePassengerDto getCreatePassengerDto(UserAccount userAccount) {
        CreatePassengerDto createPassengerDto = new CreatePassengerDto();
        Passenger passenger = userAccount.getPassenger();

        if (passenger != null && passenger.getFirstName() != null) {
            createPassengerDto.setFirstName(passenger.getFirstName());
            createPassengerDto.setLastName(passenger.getLastName());
            createPassengerDto.setMiddleName(passenger.getMiddleName());
            createPassengerDto.setGender(passenger.getGender());
            createPassengerDto.setBirthDate(passenger.getBirthDate());
            createPassengerDto.setDocumentType(passenger.getDocumentType());
            createPassengerDto.setDocumentNumber(passenger.getDocumentNumber());
            createPassengerDto.setContactEmail(passenger.getContactEmail());
            createPassengerDto.setContactPhone(passenger.getContactPhone());
        }

        return createPassengerDto;
    }

    @PostMapping("/order/services")
    public String addServicesToOrder(
            @RequestParam("orderId") Long orderId,
            @RequestParam(value = "serviceIds", required = false) List<Long> serviceIds) {
        bookingService.addServicesToOrder(orderId, serviceIds);
        return "redirect:/account";
    }

    @PostMapping("/order/refund")
    public String refundOrder(@RequestParam("orderId") Long orderId) {
        bookingService.refundBookingOrder(orderId);
        return "redirect:/account";
    }

    @PostMapping("/save-passenger")
    public String savePassenger(@Valid @ModelAttribute CreatePassengerDto createPassengerDto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model) {
        if (bindingResult.hasErrors()) {
            UserAccount userAccount = userAccountService.findByIdWithTransactions(userDetails.getId());
            model.addAttribute("userAccount", userAccount);
            model.addAttribute("activeTab", "documents");

            return "account/main";
        }

        userAccountService.savePassengerData(userDetails.getId(), createPassengerDto);
        return "redirect:/account/documents?saved=true";
    }

    @PostMapping("/delete-passenger")
    public String deletePassenger(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userAccountService.deletePassengerData(userDetails.getId());
        return "redirect:/account/documents?deleted=true";
    }
}
