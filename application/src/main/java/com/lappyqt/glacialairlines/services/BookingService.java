package com.lappyqt.glacialairlines.services;

import com.lappyqt.glacialairlines.entities.account.LoyaltyAccount;
import com.lappyqt.glacialairlines.entities.account.LoyaltyTransaction;
import com.lappyqt.glacialairlines.entities.account.Passenger;
import com.lappyqt.glacialairlines.entities.account.UserAccount;
import com.lappyqt.glacialairlines.entities.booking.AdditionalService;
import com.lappyqt.glacialairlines.entities.booking.BookingOrder;
import com.lappyqt.glacialairlines.entities.booking.OrderPassenger;
import com.lappyqt.glacialairlines.entities.flight.*;
import com.lappyqt.glacialairlines.enums.*;
import com.lappyqt.glacialairlines.exceptions.RefundUnavailableException;
import com.lappyqt.glacialairlines.exceptions.SeatAlreadyOccupiedException;
import com.lappyqt.glacialairlines.repositories.account.LoyaltyTransactionRepository;
import com.lappyqt.glacialairlines.repositories.booking.AdditionalServiceRepository;
import com.lappyqt.glacialairlines.repositories.booking.BookingOrderRepository;
import com.lappyqt.glacialairlines.repositories.flight.FlightInventoryRepository;
import com.lappyqt.glacialairlines.repositories.flight.FlightRepository;
import com.lappyqt.glacialairlines.repositories.flight.SeatAvailabilityRepository;
import dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

// Сервисный класс для управления процессами бронирования авиабилетов, доп. услуг, оплаты и возвратов
@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {
    private final AdditionalServiceRepository additionalServiceRepository;
    private final FlightRepository flightRepository;
    private final BookingOrderRepository bookingOrderRepository;
    private final SeatAvailabilityRepository seatAvailabilityRepository;
    private final FlightInventoryRepository flightInventoryRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;

    // Метод для получения информации о заказе по его идентификатору
    @Transactional(readOnly = true)
    public BookingOrder getOrder(Long orderId) {
        return fetchFullOrderGraph(orderId); // Подгружаем все связные таблицы
    }

    // Метод для создания нового или обновления существующего черновика заказа
    @Transactional
    public BookingOrder getOrCreateDraft(Long orderId, Long outboundFlightId, Long returnFlightId,
                                         SearchRequestDto searchRequest, UserAccount userAccount) {
        // Обновляем данные уже существующего заказа
        if (orderId != null) {
            BookingOrder order = bookingOrderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Заказ (%d) не найден", orderId)));

            // Обновление ссылок на рейсы "туда" и "обратно"
            order.setOutboundFlight(flightRepository.getReferenceById(outboundFlightId));
            order.setReturnFlight(returnFlightId != null
                ? flightRepository.getReferenceById(returnFlightId)
                : null);

            // Синхронизация количества пассажиров в соотвествии с поисковым запросом
            syncPassengers(order, searchRequest, userAccount);
            return bookingOrderRepository.save(order);
        }

        // Создаем новый черновик, если заказа нет
        return bookingOrderRepository.save(
                createNewDraft(userAccount, outboundFlightId, returnFlightId, searchRequest)
        );
    }

    // Получаем список всех активных дополнительных услуг от авиакомпании
    @Transactional(readOnly = true)
    public List<AdditionalService> getAdditionalServices() {
        return additionalServiceRepository.findByIsActiveTrue();
    }

    // Метод создания нового черновика заказа
    private BookingOrder createNewDraft(UserAccount userAccount, Long outboundFlightId, Long returnFlightId, SearchRequestDto searchRequest) {
        BookingOrder order = new BookingOrder();
        order.setUserAccount(userAccount);
        order.setOutboundFlight(flightRepository.getReferenceById(outboundFlightId));
        order.setReturnFlight(returnFlightId != null
                ? flightRepository.getReferenceById(returnFlightId)
                : null);
        order.setSeatClass(searchRequest.getServiceClass());
        order.setStatus(OrderStatus.DRAFT);
        order.setTotalPrice(BigDecimal.ZERO);
        order.setCreatedAt(Instant.now());

        // Время жизни бронирования/черновика устанавливается на 30 минут
        order.setBookingExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));

        // Наполняем черновика пассажирами
        syncPassengers(order, searchRequest, userAccount);
        return order;
    }

    // Метод для сохранения персональных данных пассажиров и фиксации базовой стоимости билетов
    @Transactional
    public void savePassengersAndSetBasePrice(Long orderId, BigDecimal basePrice, PassengersFormDto passengersFormDto) {
        BookingOrder order = bookingOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Заказ (%d) не найден", orderId)));

        // Сохранение контактных данных для связи по заказу
        order.setContactEmail(passengersFormDto.getContactEmail().toLowerCase().trim());
        order.setContactPhone(passengersFormDto.getContactPhone().trim());
        order.setBasePrice(basePrice);

        // Построчный маппинг данных из формы в сущности пассажиров заказа
        for (int i = 0; i < passengersFormDto.getPassengers().size(); i++) {
            PassengerDto dto = passengersFormDto.getPassengers().get(i);
            OrderPassenger passenger = order.getPassengers().get(i);

            passenger.setFirstName(dto.getFirstName().trim());
            passenger.setLastName(dto.getLastName().trim());
            passenger.setMiddleName(dto.getMiddleName().trim());
            passenger.setGender(dto.getGender());
            passenger.setBirthDate(dto.getBirthDate());
            passenger.setDocumentType(dto.getDocumentType());

            // Очистка номера документа от случайных пробелов
            passenger.setDocumentNumber(dto.getDocumentNumber().trim().replaceAll("\\s+", ""));
            passenger.setPassengerType(dto.getPassengerType());
        }

        bookingOrderRepository.save(order);
    }

    // Вспомогательный метод для приведения списка пассажиров в заказе в соответствие с поисковым запросом
    private void syncPassengers(BookingOrder order, SearchRequestDto searchRequest, UserAccount userAccount) {
        int adultsCountRequired = searchRequest.getAdultsCount();
        int childrenCountRequired = searchRequest.getChildrenCount();

        // Подсчет текущего количества взрослых и детей в объекте заказа
        long currentAdultsCount = order.getPassengers().stream()
                .filter(p -> p.getPassengerType() == PassengerType.ADULT).count();
        long currentChildrenCount = order.getPassengers().stream()
                .filter(p -> p.getPassengerType() == PassengerType.CHILD).count();

        if (currentAdultsCount != adultsCountRequired || currentChildrenCount != childrenCountRequired) {
            order.getPassengers().clear();

            boolean isFirstAdult = true;

            // Заполнение слотов для взрослых пассажиров
            for (long i = 0; i < adultsCountRequired; i++) {
                OrderPassenger adultPassenger = new OrderPassenger();
                adultPassenger.setOrder(order);
                adultPassenger.setPassengerType(PassengerType.ADULT);
                order.getPassengers().add(adultPassenger);

                // Автоматическое заполнение данных первого пассажира из профиля авторизованного пользователя
                if (isFirstAdult && userAccount != null && userAccount.getPassenger() != null && userAccount.getPassenger().getFirstName() != null) {
                    mapProfileToOrderPassenger(userAccount.getPassenger(), adultPassenger);
                    isFirstAdult = false;
                }
            }

            // Заполнение слотов для детей
            for (long i = 0; i < childrenCountRequired; i++) {
                OrderPassenger childPassenger = new OrderPassenger();
                childPassenger.setOrder(order);
                childPassenger.setPassengerType(PassengerType.CHILD);
                order.getPassengers().add(childPassenger);
            }
        }
    }

    // Вспомогательный метод для копирования персональных данных из профиля пользователя в форму пассажира в заказе
    private void mapProfileToOrderPassenger(Passenger profilePassenger, OrderPassenger orderPassenger) {
        orderPassenger.setFirstName(profilePassenger.getFirstName());
        orderPassenger.setLastName(profilePassenger.getLastName());
        orderPassenger.setMiddleName(profilePassenger.getMiddleName());
        orderPassenger.setGender(profilePassenger.getGender());
        orderPassenger.setBirthDate(profilePassenger.getBirthDate());
        orderPassenger.setDocumentType(profilePassenger.getDocumentType());
        orderPassenger.setDocumentNumber(profilePassenger.getDocumentNumber());
    }

    // Метод для привязки выбранных услуг, мест и резервирования инвентарных мест рейса перед оплатой
    @Transactional
    public PrepareCheckoutResponseDto saveServicesAndPrepareCheckout(Long orderId, ServicesFormDto form) {
        BookingOrder order = fetchFullOrderGraph(orderId);

        // Обновление списка выбранных дополнительных услуг
        if (form.getSelectedServiceIds() != null && !form.getSelectedServiceIds().isEmpty()) {
            List<AdditionalService> services = additionalServiceRepository.findAllById(form.getSelectedServiceIds());
            order.getSelectedServices().clear();
            order.getSelectedServices().addAll(services);
        } else {
            order.getSelectedServices().clear();
        }

        List<OrderPassenger> passengers = order.getPassengers();
        BigDecimal seatsSurcharge = BigDecimal.ZERO;

        // Обработка логики выбора мест в самолете
        if (form.isSkipSeats()) {
            passengers.forEach(p -> p.setOutboundSeatAvailability(null));
        }
        else if (form.getOutboundSeatIds() != null && !form.getOutboundSeatIds().isEmpty()) {
            List<Long> seatIds = form.getOutboundSeatIds().stream()
                    .filter(id -> id != null && id != 0)
                    .collect(Collectors.toList());

            if (!seatIds.isEmpty()) {
                Map<Long, SeatAvailability> seatMap = seatAvailabilityRepository.findByIdsWithSeat(seatIds)
                        .stream()
                        .collect(Collectors.toMap(SeatAvailability::getId, sa -> sa));

                for (int i = 0; i < passengers.size() && i < form.getOutboundSeatIds().size(); i++) {
                    Long seatAvailabilityId = form.getOutboundSeatIds().get(i);

                    if (seatAvailabilityId != null && seatAvailabilityId != 0) {
                        SeatAvailability sa = seatMap.get(seatAvailabilityId);

                        if (sa != null) {
                            passengers.get(i).setOutboundSeatAvailability(sa);

                            // Доплата за места у аварийного выхода
                            if (sa.getSeat().getSeatClass() == SeatClass.EMERGENCY) {
                                seatsSurcharge = seatsSurcharge.add(BigDecimal.valueOf(400));
                            }
                        }
                    }
                }
            }
        }

        // Подсчет стоимости выбранных доп. услуг
        BigDecimal servicesTotal = order.getSelectedServices().stream()
                .map(AdditionalService::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Расчет финальной стоимости заказа
        BigDecimal total = order.getBasePrice()
                .add(seatsSurcharge)
                .add(servicesTotal);

        // Если заказ переводится на этап оплаты впервые, списываем (бронируем) места в инвентаре рейсов
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            // Блокировка инвентаря вылетающего рейса для предотвращения race condition
            FlightInventory inventory = flightInventoryRepository
                    .findByFlightIdAndSeatClassWithLock(order.getOutboundFlight().getId(), order.getSeatClass())
                    .orElseThrow(() -> new IllegalArgumentException("Инвентарь рейса не найден"));

            if (inventory.getAvailableSeats() < order.getPassengers().size()) {
                throw new IllegalStateException("Недостаточно свободных мест на вылетающем рейсе");
            }

            // Уменьшение количества доступных мест
            inventory.setAvailableSeats(inventory.getAvailableSeats() - order.getPassengers().size());
            flightInventoryRepository.save(inventory);

            // Аналогичное бронирование мест для обратного рейса при его наличии
            if (order.getReturnFlight() != null) {
                FlightInventory returnInventory = flightInventoryRepository
                        .findByFlightIdAndSeatClassWithLock(order.getReturnFlight().getId(), order.getSeatClass())
                        .orElseThrow(() -> new IllegalArgumentException("Инвентарь обратного рейса не найден"));

                if (returnInventory.getAvailableSeats() < order.getPassengers().size()) {
                    throw new IllegalStateException("Недостаточно свободных мест на обратном рейсе");
                }

                returnInventory.setAvailableSeats(returnInventory.getAvailableSeats() - order.getPassengers().size());
                flightInventoryRepository.save(returnInventory);
            }
        }

        // Фиксация итоговой суммы и смена статуса на ожидание оплаты
        order.setTotalPrice(total);
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        bookingOrderRepository.save(order);
        return new PrepareCheckoutResponseDto(seatsSurcharge, servicesTotal);
    }

    // Метод проведения оплаты заказа с учетом списания и начисления бонусных миль
    @Transactional
    public void processPayment(Long orderId, PaymentFormDto paymentFormDto) {
        BookingOrder bookingOrder = fetchFullOrderGraph(orderId);

        // Получение ID выбранных мест для фиксации их занятости в системе
        List<Long> seatIds = bookingOrder.getPassengers().stream()
                .map(OrderPassenger::getOutboundSeatAvailability)
                .filter(Objects::nonNull)
                .map(SeatAvailability::getId)
                .toList();

        // Перевод статуса мест в OCCUPIED (Занято) с блокировкой записей
        if (!seatIds.isEmpty()) {
            List<SeatAvailability> seats = seatAvailabilityRepository.findByIdsWithLock(seatIds);

            seats.forEach(sa -> {
                if (sa.getStatus() != SeatStatus.AVAILABLE) {
                    throw new SeatAlreadyOccupiedException(sa.getSeat().getSeatNumber());
                }
                sa.setStatus(SeatStatus.OCCUPIED);
            });
        }

        LoyaltyAccount loyaltyAccount = bookingOrder.getUserAccount().getLoyaltyAccount();
        Instant now = Instant.now();

        // Сценарий оплаты накопленными милями
        if (paymentFormDto.isPayWithMiles() && loyaltyAccount.getMiles() > 0) {
            int milesSpent = loyaltyAccount.getMiles();
            BigDecimal milesDiscount = BigDecimal.valueOf(milesSpent);
            BigDecimal newTotalPrice = bookingOrder.getTotalPrice().subtract(milesDiscount).max(BigDecimal.ZERO);

            // Обновление финансовой информации заказа и обнуление баланса миль
            bookingOrder.setMilesSpent(milesSpent);
            bookingOrder.setTotalPrice(newTotalPrice);
            loyaltyAccount.setMiles(0);

            // Регистрация транзакции списания миль
            LoyaltyTransaction spentTransaction = new LoyaltyTransaction();
            spentTransaction.setLoyaltyAccount(loyaltyAccount);
            spentTransaction.setOrder(bookingOrder);
            spentTransaction.setTransactionType(LoyaltyTransactionType.SPENT);
            spentTransaction.setMiles(milesSpent);
            spentTransaction.setCreatedAt(now);
            loyaltyTransactionRepository.save(spentTransaction);
        }

        // Расчет количества начисляемых миль за текущую покупку на основе класса обслуживания
        int milesEarned = bookingOrder.getTotalPrice()
                .multiply(BigDecimal.valueOf(bookingOrder.getSeatClass().getMilesPercent() / 100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        bookingOrder.setMilesEarned(milesEarned);

        // Начисление бонусов на счет программы лояльности
        if (milesEarned > 0) {
            loyaltyAccount.setMiles(loyaltyAccount.getMiles() + milesEarned);

            // Регистрация транзакции начисления миль
            LoyaltyTransaction earnedTransaction = new LoyaltyTransaction();
            earnedTransaction.setLoyaltyAccount(loyaltyAccount);
            earnedTransaction.setOrder(bookingOrder);
            earnedTransaction.setTransactionType(LoyaltyTransactionType.EARNED);
            earnedTransaction.setMiles(milesEarned);
            earnedTransaction.setCreatedAt(now);
            loyaltyTransactionRepository.save(earnedTransaction);
        }

        // Генерация уникального идентификатора платежа (фиксация факта успешной транзакции)
        bookingOrder.setPaymentId(UUID.randomUUID().toString());
        bookingOrder.setStatus(OrderStatus.PAID);
        bookingOrderRepository.save(bookingOrder);

        log.info("Заказ {} успешно оплачен", bookingOrder.getId());
    }

    // Метод добавления дополнительных услуг к уже оплаченному заказу
    @Transactional
    public void addServicesToOrder(Long orderId, List<Long> newServiceIds) {
        BookingOrder bookingOrder = fetchFullOrderGraph(orderId);

        // Проверка временного регламента на изменение параметров бронирования
        checkServiceChangeAvailability(bookingOrder);

        List<AdditionalService> newServices = additionalServiceRepository.findAllById(newServiceIds);

        // Отсеивание услуг, которые уже привязаны к заказу
        List<AdditionalService> servicesToAdd = newServices.stream()
                .filter(service -> !bookingOrder.getSelectedServices().contains(service))
                .toList();

        // Добавление новых услуг и увеличение итоговой стоимости заказа
        if (!servicesToAdd.isEmpty()) {
            bookingOrder.getSelectedServices().addAll(servicesToAdd);

            BigDecimal extraCost = servicesToAdd.stream()
                    .map(AdditionalService::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            bookingOrder.setTotalPrice(bookingOrder.getTotalPrice().add(extraCost));
        }

        bookingOrderRepository.save(bookingOrder);
    }

    // Метод оформления возврата денежных средств и аннулирования билетов
    @Transactional
    public void refundBookingOrder(Long orderId) {
        BookingOrder bookingOrder = fetchFullOrderGraph(orderId);

        // Проверка доступности проведения операции по времени до вылета
        checkServiceChangeAvailability(bookingOrder);

        // Проверка наличия у пользователя купленной услуги "Возврат билета"
        boolean containsRefundService = bookingOrderRepository.hasRefundService(orderId);
        if (!containsRefundService) {
            throw new RefundUnavailableException("Опция возврата заказа требует приобретения соответствующей дополнительной услуги");
        }

        // Освобождение занятых мест в салоне самолета (перевод обратно в AVAILABLE)
        List<Long> seatIds = bookingOrderRepository.findPassengerSeatIds(orderId);
        if (!seatIds.isEmpty()) {
            List<SeatAvailability> seats = seatAvailabilityRepository.findByIdsWithLock(seatIds);
            seats.forEach(sa -> sa.setStatus(SeatStatus.AVAILABLE));
        }

        // Возврат инвентарных мест вылетающего вылета
        FlightInventory inventory = flightInventoryRepository
                .findByFlightIdAndSeatClassWithLock(bookingOrder.getOutboundFlight().getId(), bookingOrder.getSeatClass())
                .orElseThrow(() -> new IllegalArgumentException("Инвентарь рейса не найден"));
        inventory.setAvailableSeats(inventory.getAvailableSeats() + bookingOrder.getPassengers().size());
        flightInventoryRepository.save(inventory);

        // Возврат мест обратного рейса, если он оформлен
        if (bookingOrder.getReturnFlight() != null) {
            FlightInventory returnInventory = flightInventoryRepository
                    .findByFlightIdAndSeatClassWithLock(bookingOrder.getReturnFlight().getId(), bookingOrder.getSeatClass())
                    .orElseThrow(() -> new IllegalArgumentException("Инвентарь обратного рейса не найден"));
            returnInventory.setAvailableSeats(returnInventory.getAvailableSeats() + bookingOrder.getPassengers().size());
            flightInventoryRepository.save(returnInventory);
        }

        Instant now = Instant.now();
        LoyaltyAccount loyaltyAccount = bookingOrder.getUserAccount().getLoyaltyAccount();
        // В рамках компенсации рассчитываем эквивалент стоимости в виде бонусных миль
        int milesCount = bookingOrder.getTotalPrice().setScale(0, RoundingMode.HALF_UP).intValue();

        // Фиксация транзакции возврата в системе лояльности
        LoyaltyTransaction loyaltyTransaction = new LoyaltyTransaction();
        loyaltyTransaction.setLoyaltyAccount(loyaltyAccount);
        loyaltyTransaction.setOrder(bookingOrder);
        loyaltyTransaction.setTransactionType(LoyaltyTransactionType.RETURNED);
        loyaltyTransaction.setMiles(milesCount);
        loyaltyTransaction.setCreatedAt(now);
        loyaltyTransactionRepository.save(loyaltyTransaction);

        // Зачисление миль-компенсации на счет
        loyaltyAccount.setMiles(loyaltyAccount.getMiles() + milesCount);
        bookingOrder.setStatus(OrderStatus.RETURNED);
        bookingOrder.setReturnedAt(now);

        bookingOrderRepository.save(bookingOrder);
    }

    // Вспомогательный метод для загрузки единого связанного графа сущностей заказа (Пассажиры, Рейсы, Услуги)
    private BookingOrder fetchFullOrderGraph(Long orderId) {
        BookingOrder order = bookingOrderRepository.findByIdWithPassengersAndFlights(orderId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Заказ (%d) не найден", orderId)));
        bookingOrderRepository.findByIdWithServices(orderId);
        return order;
    }

    // Вспомогательный метод проверки бизнес-правила: любые манипуляции (возврат/доп. услуги) запрещены менее чем за 24 часа до вылета
    private void checkServiceChangeAvailability(BookingOrder order) throws IllegalStateException {
        Airport departureAirport = order.getOutboundFlight().getRoute().getDepartureAirport();

        // Учет тайм-зоны конкретного аэропорта отправления для точности сравнения
        ZoneOffset airportOffset = ZoneOffset.ofHours(departureAirport.getOffsetUTC());
        LocalDateTime nowAtDepartureAirport = LocalDateTime.now(airportOffset);
        LocalDateTime departureTime = order.getOutboundFlight().getDepartureTime();

        // Проверка временного лимита в 24 часа
        if (nowAtDepartureAirport.plusHours(24).isAfter(departureTime)) {
            throw new IllegalStateException("До вылета осталось меньше 24 часов. Изменение услуг, возврат невозможны.");
        }
    }
}
