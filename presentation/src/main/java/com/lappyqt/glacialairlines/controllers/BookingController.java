package com.lappyqt.glacialairlines.controllers;

import com.lappyqt.glacialairlines.entities.account.Passenger;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

// Контроллер для пошагового управления процессом бронирования, заполнения данных и оплаты билетов
@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {
    private final FlightService flightService;
    private final BookingService bookingService;
    private final UserAccountService userAccountService;
    private final BookingSession bookingSession;

    // Метод отображения страницы выбора обратного рейса (для маршрутов типа "Туда и обратно")
    @GetMapping("/return-flight")
    public String returnFlightPage(Model model, @RequestParam(value = "filter", required = false, defaultValue = "price") String filter) {
        SearchRequestDto searchRequest = bookingSession.getSearchRequest();
        model.addAttribute("searchRequestDto", searchRequest);
        model.addAttribute("filter", filter);

        // Получение информации о ранее выбранном рейсе "туда" для отображения в закрепленной карточке
        SearchResponseDto outboundFlight = flightService.getFlightOffer(bookingSession.getOutboundFlightId(), searchRequest);
        model.addAttribute("outboundFlight", outboundFlight);

        Integer milesObject = (Integer) model.getAttribute("currentMiles");
        int currentMiles = (milesObject != null) ? milesObject : 0;

        // Поиск доступных вариантов для обратного перелета
        List<SearchResponseDto> returnFlights = flightService.findAvailableReturnFlightOffers(searchRequest, currentMiles, filter);

        // Вычисление минимальной стоимости обратного перелета
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

    // Метод фиксации выбранного обратного рейса в сессии и перехода к вводу данных пассажиров
    @GetMapping("select-return")
    public String selectReturn(@RequestParam Long returnFlightId) {
        bookingSession.setReturnFlightId(returnFlightId);
        bookingSession.setReturnOfferPrice(flightService.getFlightOffer(returnFlightId, bookingSession.getSearchRequest()).getTotalPrice());

        return "redirect:/booking/passengers";
    }

    // Метод отображения страницы ввода персональных и паспортных данных пассажиров
    @GetMapping("/passengers")
    public String passengersPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("searchRequestDto", bookingSession.getSearchRequest());

        // Получение профиля пользователя для автозаполнения первого слота пассажира
        UserAccount userAccount = userAccountService.findById(userDetails.getId());
        Passenger profilePassenger = userAccount.getPassenger();

        // Создание или получение существующего в БД черновика заказа
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

        // Расчет базовой суммарной стоимости авиабилетов за все сегменты полета
        model.addAttribute("basePrice", bookingSession.getOutboundOfferPrice().add(
                returnFlightPrice != null ? returnFlightPrice : BigDecimal.ZERO
        ));

        model.addAttribute("isRoundTrip", bookingSession.getReturnFlightId() != null);

        // Инициализация DTO-формы значениями из черновика или профиля пользователя по умолчанию
        PassengersFormDto form = new PassengersFormDto();
        form.setContactEmail(order.getContactEmail() == null ? profilePassenger.getContactEmail() : order.getContactEmail());
        form.setContactPhone(order.getContactPhone() == null ? profilePassenger.getContactPhone() : order.getContactPhone());
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

    // Метод обработки и сохранения формы с личными данными пассажиров билета
    @PostMapping("/passengers")
    public String savePassengers(@Valid @ModelAttribute PassengersFormDto form,
                                 BindingResult bindingResult,
                                 Model model) {
        // Проверка корректности заполнения обязательных полей (ФИО, документы)
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

        // Вычисление и фиксация итоговой базовой цены билетов в сервисе
        BigDecimal returnFlightPrice = bookingSession.getReturnOfferPrice();
        BigDecimal basePrice = bookingSession.getOutboundOfferPrice().add(
                returnFlightPrice != null ? returnFlightPrice : BigDecimal.ZERO
        );

        bookingService.savePassengersAndSetBasePrice(bookingSession.getOrderId(), basePrice, form);
        return "redirect:/booking/services";
    }

    // Метод отображения страницы выбора мест на интерактивной схеме салона и выбора доп. услуг
    @GetMapping("/services")
    public String servicesPage(Model model) {
        BookingOrder bookingOrder = bookingService.getOrder(bookingSession.getOrderId());
        // Получение сгруппированной структуры свободных и занятых мест по рядам
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

    // Метод сохранения выбранных мест и услуг с пересчетом наценок и переходом на оплату
    @PostMapping("/services")
    public String saveSelectedServices(@ModelAttribute ServicesFormDto servicesFormDto) {
        // Подготовка данных к оплате и бронирование инвентарных мест
        PrepareCheckoutResponseDto prepareCheckoutResponseDto = bookingService.saveServicesAndPrepareCheckout(bookingSession.getOrderId(), servicesFormDto);
        bookingSession.setSeatsSurcharge(prepareCheckoutResponseDto.getSeatsSurcharge());
        bookingSession.setServicesTotal(prepareCheckoutResponseDto.getServicesTotal());

        return "redirect:/booking/checkout";
    }

    // Метод отображения финальной страницы подтверждения заказа и ввода платежных реквизитов
    @GetMapping("/checkout")
    public String checkoutPage(Model model) {
        BookingOrder order = bookingService.getOrder(bookingSession.getOrderId());
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

    // Метод проведения транзакции оплаты заказа (списание/начисление миль, занятие физических мест)
    @PostMapping("/checkout")
    public String processPayment(@Valid @ModelAttribute PaymentFormDto paymentFormDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttribute,
                                 Model model) {
        // Проверка корректности реквизитов банковской карты
        if (bindingResult.hasErrors()) {
            BookingOrder order = bookingService.getOrder(bookingSession.getOrderId());
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


        Long orderId = bookingSession.getOrderId();
        // Запуск транзакции эквайринга
        bookingService.processPayment(bookingSession.getOrderId(), paymentFormDto);
        // Сброс контекста завершенного бронирования из сессии
        bookingSession.clear();

        redirectAttribute.addAttribute("orderId", orderId);

        return "redirect:/booking/success";
    }

    // Метод отображения страницы успешного бронирования заказа
    @GetMapping("/success")
    public String successPage(@RequestParam(name = "orderId") Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "booking/success";
    }
}
