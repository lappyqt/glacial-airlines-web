package com.lappyqt.glacialairlines.controllers;

import com.lappyqt.glacialairlines.entities.account.LoyaltyTransaction;
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

// Контроллер для управления личным кабинетом пользователя, его заказами, документами и историей лояльности
@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final UserAccountService userAccountService;
    private final BookingService bookingService;

    // Метод перенаправления с базового URL личного кабинета на вкладку по умолчанию ("orders")
    @GetMapping
    public String accountPageDefault(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        return accountPage("orders", userDetails, model);
    }

    // Метод отображения контента личного кабинета в зависимости от выбранной вкладки
    @GetMapping(value = {"/", "/{tab}"})
    public String accountPage(@PathVariable(required = false, name = "tab")
                              String tab,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        String activeTab = (tab != null && !tab.isEmpty()) ? tab : "orders";
        model.addAttribute("activeTab", activeTab);

        // Загрузка данных аккаунта текущего авторизованного пользователя
        UserAccount userAccount = userAccountService.findByIdWithPassengerAndLoyalty(userDetails.getId());
        model.addAttribute("userAccount", userAccount);

        // Наполнение модели данными в зависимости от активного раздела личного кабинета
        if (activeTab.equalsIgnoreCase("orders")) {
            List<BookingOrderDto> bookingOrders = userAccountService.getUserOrders(userDetails.getId());
            model.addAttribute("bookingOrders", bookingOrders);
        }

        if (activeTab.equalsIgnoreCase("documents")) {
            CreatePassengerDto createPassengerDto = getCreatePassengerDto(userAccount);
            model.addAttribute("createPassengerDto", createPassengerDto);
        }

        if (activeTab.equalsIgnoreCase("history")) {
            List<LoyaltyTransaction> transactions = userAccountService.getUserTransactions(userAccount.getLoyaltyAccount().getId());
            model.addAttribute("transactions", transactions);
        }

        return "account/main";
    }

    // Вспомогательный метод маппинга данных пассажира профиля в форму редактирования документов
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

    // Метод добавления (докупки) дополнительных услуг к уже оформленному и оплаченному билету
    @PostMapping("/order/services")
    public String addServicesToOrder(
            @RequestParam("orderId") Long orderId,
            @RequestParam(value = "serviceIds", required = false) List<Long> serviceIds) {
        bookingService.addServicesToOrder(orderId, serviceIds);
        return "redirect:/account";
    }

    // Метод инициации процесса аннулирования билета и оформления возврата средств
    @PostMapping("/order/refund")
    public String refundOrder(@RequestParam("orderId") Long orderId) {
        bookingService.refundBookingOrder(orderId);
        return "redirect:/account";
    }

    // Метод сохранения или изменения постоянных данных пользователя (как пассажира) в профиле
    @PostMapping("/save-passenger")
    public String savePassenger(@Valid @ModelAttribute CreatePassengerDto createPassengerDto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model) {
        // Контроль корректности заполнения формы пассажира
        if (bindingResult.hasErrors()) {
            UserAccount userAccount = userAccountService.findByIdWithPassengerAndLoyalty(userDetails.getId());
            model.addAttribute("userAccount", userAccount);
            model.addAttribute("activeTab", "documents");

            return "account/main";
        }

        userAccountService.savePassengerData(userDetails.getId(), createPassengerDto);
        return "redirect:/account/documents?saved=true";
    }

    // Метод удаления данных пассажира по умолчанию из профиля пользователя
    @PostMapping("/delete-passenger")
    public String deletePassenger(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userAccountService.deletePassengerData(userDetails.getId());
        return "redirect:/account/documents?deleted=true";
    }
}
