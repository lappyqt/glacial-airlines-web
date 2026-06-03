package com.lappyqt.glacialairlines.services;

import com.lappyqt.glacialairlines.entities.account.LoyaltyTransaction;
import com.lappyqt.glacialairlines.entities.account.Passenger;
import com.lappyqt.glacialairlines.entities.account.UserAccount;
import com.lappyqt.glacialairlines.entities.booking.AdditionalService;
import com.lappyqt.glacialairlines.entities.booking.BookingOrder;
import com.lappyqt.glacialairlines.entities.flight.Airport;
import com.lappyqt.glacialairlines.entities.flight.Flight;
import com.lappyqt.glacialairlines.entities.flight.FlightInventory;
import com.lappyqt.glacialairlines.entities.flight.Route;
import com.lappyqt.glacialairlines.enums.AdditionalServiceType;
import com.lappyqt.glacialairlines.exceptions.EmailAlreadyExistsException;
import com.lappyqt.glacialairlines.exceptions.PhoneAlreadyExistsException;
import com.lappyqt.glacialairlines.repositories.account.LoyaltyTransactionRepository;
import com.lappyqt.glacialairlines.repositories.account.UserAccountRepository;
import com.lappyqt.glacialairlines.repositories.booking.AdditionalServiceRepository;
import com.lappyqt.glacialairlines.repositories.booking.BookingOrderRepository;
import com.lappyqt.glacialairlines.repositories.flight.FlightInventoryRepository;
import dto.BookingOrderDto;
import dto.CreateAccountDto;
import dto.CreatePassengerDto;
import dto.SearchResponseDto;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Сервисный класс для управления учетными записями пользователей, профилями пассажиров и историей заказов
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountService {
    private final UserAccountRepository userAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final AdditionalServiceRepository additionalServiceRepository;
    private final PasswordEncoder passwordEncoder;

    private final FlightInventoryRepository flightInventoryRepository;
    private final BookingOrderRepository bookingOrderRepository;

    // Метод для регистрации нового пользователя в системе авиакомпании
    @Transactional
    public void createUserAccount(CreateAccountDto createAccountDto) {
        // Проверка уникальности Email адреса
        if (userAccountRepository.existsByEmail(createAccountDto.getEmail())) {
            log.warn("Email уже занят: {}", createAccountDto.getEmail());
            throw new EmailAlreadyExistsException(createAccountDto.getEmail());
        }

        // Проверка уникальности номера телефона
        if (userAccountRepository.existsByPhoneNumber(createAccountDto.getPhoneNumber())) {
            log.warn("Номер телефона уже занят: {}", createAccountDto.getPhoneNumber());
            throw new PhoneAlreadyExistsException(createAccountDto.getPhoneNumber());
        }

        // Построение сущности аккаунта с нормализацией строк (приведение ФИО к заглавным буквам, очистка пробелов)
        UserAccount userAccount = UserAccount.builder()
                .lastName(StringUtils.capitalize(createAccountDto.getLastName().trim()))
                .firstName(StringUtils.capitalize(createAccountDto.getFirstName().trim()))
                .middleName(StringUtils.capitalize(createAccountDto.getMiddleName().trim()))
                .email(createAccountDto.getEmail().toLowerCase().trim())
                .phoneNumber(createAccountDto.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(createAccountDto.getPassword()))
                .emailVerified(false)
                .authProvider(com.lappyqt.glacialairlines.enums.AuthProvider.EMAIL)
                .build();

        userAccountRepository.save(userAccount);
        log.info("Аккаунт успешно создан для email: {}", createAccountDto.getEmail());
    }

    // Метод поиска учетной записи по идентификатору (вместе со связанной сущностью пассажира)
    @Transactional(readOnly = true)
    public UserAccount findById(Long id) {
        return userAccountRepository.findByIdWithPassenger(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Аккаунт с id %d не найден", id)));
    }

    // Метод получения аккаунта по идентификатору (вместе со сущностями профиля пассажира и аккаунта программы лояльности)
    @Transactional(readOnly = true)
    public UserAccount findByIdWithPassengerAndLoyalty(Long id) {
        return userAccountRepository.findByIdWithPassengerAndLoyalty(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Аккаунт с id %d не найден", id)));
    }

    // Метод получения истории транзакций бонусных миль личного кабинета
    @Transactional(readOnly = true)
    public List<LoyaltyTransaction> getUserTransactions(Long accountId) {
        return loyaltyTransactionRepository.findByLoyaltyAccountIdWithOrder(accountId);
    }

    // Метод формирования списка заказов пользователя с расчетом доступных действий и доп. услуг
    @Transactional(readOnly = true)
    public List<BookingOrderDto> getUserOrders(Long userId) {
        // Загрузка базового списка заказов пользователя
        List<BookingOrder> bookingOrders = bookingOrderRepository.findOrdersByUserId(userId);
        List<Long> bookingOrderIds = bookingOrders.stream().map(BookingOrder::getId).toList();

        // Пакетное извлечение выбранных услуг
        Map<Long, List<AdditionalService>> servicesByOrderId =
                bookingOrderRepository.findOrdersWithServices(bookingOrderIds)
                        .stream()
                        .collect(Collectors.toMap(BookingOrder::getId, BookingOrder::getSelectedServices));

        List<AdditionalService> allAdditionalServices = additionalServiceRepository.findByIsActiveTrue();

        // Обогащение данных каждого заказа бизнес-логикой для отображения в UI
        return bookingOrders.stream().map(bookingOrder -> {
            // Определение локального времени в аэропорту вылета для контроля ограничений по времени
            Airport departureAirport = bookingOrder.getOutboundFlight().getRoute().getDepartureAirport();
            ZoneOffset airportOffset = ZoneOffset.ofHours(departureAirport.getOffsetUTC());
            LocalDateTime nowAtDepartureAirport = LocalDateTime.now(airportOffset);
            LocalDateTime departureTime = bookingOrder.getOutboundFlight().getDepartureTime();

            List<AdditionalService> selectedServices = servicesByOrderId.getOrDefault(bookingOrder.getId(), List.of());

            // Проверка правила: изменять заказ можно не позднее чем за 24 часа до рейса
            boolean isEditable = nowAtDepartureAirport.plusHours(24).isBefore(departureTime);
            // Проверка, была ли ранее приобретена услуга возможности платного возврата
            boolean refundAvailable = selectedServices.stream()
                    .anyMatch(service -> service.getServiceType() == AdditionalServiceType.REFUND);

            // Формирование перечня услуг, которые пользователь еще может докупить к билету
            List<AdditionalService> availableServices;
            if (!isEditable) {
                availableServices = List.of(); // Время вышло — изменять состав услуг нельзя
            } else {
                availableServices = selectedServices.isEmpty()
                        ? allAdditionalServices
                        : getAvailableAdditionalServices(allAdditionalServices, selectedServices);
            }

            // Маппинг данных рейса "туда" для карточки заказа
            SearchResponseDto outboundFlight = mapFlightInventoryToShortResponseDto(
                    flightInventoryRepository.findByFlightIdAndSeatClass(
                            bookingOrder.getOutboundFlight().getId(), bookingOrder.getSeatClass()
                    ).orElseThrow()
            );

            // Маппинг данных обратного рейса (при его наличии в билете)
            SearchResponseDto returnFlight = null;
            if (bookingOrder.getReturnFlight() != null && bookingOrder.getReturnFlight().getId() != null) {
                returnFlight = mapFlightInventoryToShortResponseDto(
                        flightInventoryRepository.findByFlightIdAndSeatClass(
                                bookingOrder.getReturnFlight().getId(), bookingOrder.getSeatClass()
                        ).orElseThrow()
                );
            }

            // Сборка готового DTO-объекта заказа для интерфейса
            return BookingOrderDto.builder()
                    .bookingOrder(bookingOrder)
                    .outboundFlight(outboundFlight)
                    .returnFlight(returnFlight)
                    .availableServices(availableServices)
                    .isEditable(isEditable)
                    .refundAvailable(refundAvailable)
                    .build();
        }).toList();
    }

    // Вспомогательный метод для фильтрации списка услуг (исключение уже купленных из общего каталога)
    private List<AdditionalService> getAvailableAdditionalServices(List<AdditionalService> allServices, List<AdditionalService> selectedServices) {
        Set<Long> selectedIds = selectedServices.stream()
                .map(AdditionalService::getId)
                .collect(Collectors.toSet());

        return allServices.stream()
                .filter(service -> !selectedIds.contains(service.getId()))
                .toList();
    }

    // Метод сохранения и обновления данных пользователя по умолчанию (автозаполнение)
    @Transactional
    public void savePassengerData(Long userId, CreatePassengerDto createPassengerDto) {
        UserAccount userAccount = userAccountRepository.findByIdWithPassengerAndLoyalty(userId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Пользователь с id %d не найден",userId)));

        Passenger passenger = userAccount.getPassenger();

        // Если профиль пустой — инициализируем новую сущность
        if (passenger == null) {
            passenger = new Passenger();
            userAccount.setPassenger(passenger);
        }

        // Обновление реквизитов документов и контактной информации
        passenger.setFirstName(createPassengerDto.getFirstName());
        passenger.setLastName(createPassengerDto.getLastName());
        passenger.setMiddleName(createPassengerDto.getMiddleName());
        passenger.setGender(createPassengerDto.getGender());
        passenger.setBirthDate(createPassengerDto.getBirthDate());
        passenger.setDocumentType(createPassengerDto.getDocumentType());
        passenger.setDocumentNumber(createPassengerDto.getDocumentNumber());
        passenger.setContactEmail(createPassengerDto.getContactEmail());
        passenger.setContactPhone(createPassengerDto.getContactPhone());
    }

    // Метод очистки (удаления) сохраненных личных и паспортных данных пользователя
    @Transactional
    public void deletePassengerData(Long userId) {
        UserAccount userAccount = userAccountRepository.findByIdWithPassengerAndLoyalty(userId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Пользователь с id %d не найден",userId)));

        // Сброс полей в null без физического удаления связки аккаунтов
        Passenger passenger = userAccount.getPassenger();
        if (passenger != null) {
            passenger.setFirstName(null);
            passenger.setLastName(null);
            passenger.setMiddleName(null);
            passenger.setGender(null);
            passenger.setBirthDate(null);
            passenger.setDocumentType(null);
            passenger.setDocumentNumber(null);
            passenger.setContactEmail(null);
            passenger.setContactPhone(null);
        }
    }

    // Вспомогательный метод для маппинга инвентарных данных рейса в сокращенное DTO-представление билета
    private SearchResponseDto mapFlightInventoryToShortResponseDto(FlightInventory flightInventory) {
        Flight flight = flightInventory.getFlight();
        Route route = flight.getRoute();

        LocalDateTime departureUTC = flight.getDepartureTime()
                .minusHours(route.getDepartureAirport().getOffsetUTC());

        LocalDateTime arrivalUTC = flight.getArrivalTime()
                .minusHours(route.getArrivalAirport().getOffsetUTC());

        Duration flightDuration = Duration.between(departureUTC, arrivalUTC);

        return SearchResponseDto.builder()
                .flightId(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .flightDuration(flightDuration)
                .aircraftModel(flight.getAircraft().getModel())
                .departureIataCode(route.getDepartureAirport().getIataCode())
                .arrivalIataCode(route.getArrivalAirport().getIataCode())
                .departureCity(route.getDepartureAirport().getCity())
                .arrivalCity(route.getArrivalAirport().getCity())
                .seatClass(flightInventory.getSeatClass())
                .availableSeats(flightInventory.getAvailableSeats())
                .build();
    }
}
