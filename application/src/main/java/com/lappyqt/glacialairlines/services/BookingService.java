package com.lappyqt.glacialairlines.services;

import com.lappyqt.glacialairlines.entities.account.LoyaltyAccount;
import com.lappyqt.glacialairlines.entities.account.LoyaltyTransaction;
import com.lappyqt.glacialairlines.entities.account.UserAccount;
import com.lappyqt.glacialairlines.entities.booking.AdditionalService;
import com.lappyqt.glacialairlines.entities.booking.BookingOrder;
import com.lappyqt.glacialairlines.entities.booking.OrderPassenger;
import com.lappyqt.glacialairlines.entities.flight.FlightInventory;
import com.lappyqt.glacialairlines.entities.flight.SeatAvailability;
import com.lappyqt.glacialairlines.enums.*;
import com.lappyqt.glacialairlines.exceptions.SeatAlreadyOccupiedException;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {
    private final AdditionalServiceRepository additionalServiceRepository;
    private final FlightRepository flightRepository;
    private final BookingOrderRepository bookingOrderRepository;
    private final SeatAvailabilityRepository seatAvailabilityRepository;
    private final FlightInventoryRepository flightInventoryRepository;

    @Transactional(readOnly = true)
    public BookingOrder getOrder(Long orderId) {
        BookingOrder order = bookingOrderRepository.findByIdWithPassengers(orderId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Заказ (%d) не найден", orderId)));

        bookingOrderRepository.findByIdWithServices(orderId);
        return order;
    }

    @Transactional(readOnly = true)
    public BookingOrder getFullOrder(Long orderId) {
        BookingOrder order = bookingOrderRepository.findByIdWithPassengersAndFlights(orderId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Заказ (%d) не найден", orderId)));

        bookingOrderRepository.findByIdWithServices(orderId);
        return order;
    }

    @Transactional
    public BookingOrder getOrCreateDraft(Long orderId, Long outboundFlightId, Long returnFlightId,
                                         SearchRequestDto searchRequest, UserAccount userAccount) {
        if (orderId != null) {
            BookingOrder order = bookingOrderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Заказ (%d) не найден", orderId)));

            order.setOutboundFlight(flightRepository.getReferenceById(outboundFlightId));
            order.setReturnFlight(returnFlightId != null
                ? flightRepository.getReferenceById(returnFlightId)
                : null);

            syncPassengers(order, searchRequest);
            return bookingOrderRepository.save(order);
        }

        return bookingOrderRepository.save(
                createNewDraft(userAccount, outboundFlightId, returnFlightId, searchRequest)
        );
    }

    @Transactional(readOnly = true)
    public List<AdditionalService> getAdditionalServices() {
        return additionalServiceRepository.findByIsActiveTrue();
    }

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
        order.setBookingExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));

        syncPassengers(order, searchRequest);
        return order;
    }

    @Transactional
    public void savePassengersAndSetBasePrice(Long orderId, BigDecimal basePrice, PassengersFormDto passengersFormDto) {
        BookingOrder order = bookingOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Заказ (%d) не найден", orderId)));

        order.setContactEmail(passengersFormDto.getContactEmail().toLowerCase().trim());
        order.setContactPhone(passengersFormDto.getContactPhone().trim());
        order.setBasePrice(basePrice);

        for (int i = 0; i < passengersFormDto.getPassengers().size(); i++) {
            PassengerDto dto = passengersFormDto.getPassengers().get(i);
            OrderPassenger passenger = order.getPassengers().get(i);

            passenger.setFirstName(dto.getFirstName().trim());
            passenger.setLastName(dto.getLastName().trim());
            passenger.setMiddleName(dto.getMiddleName().trim());
            passenger.setGender(dto.getGender());
            passenger.setBirthDate(dto.getBirthDate());
            passenger.setDocumentType(dto.getDocumentType());
            passenger.setDocumentNumber(dto.getDocumentNumber().trim().replaceAll("\\s+", ""));
            passenger.setPassengerType(dto.getPassengerType());
        }

        bookingOrderRepository.save(order);
    }

    private void syncPassengers(BookingOrder order, SearchRequestDto searchRequest) {
        int adultsCountRequired = searchRequest.getAdultsCount();
        int childrenCountRequired = searchRequest.getChildrenCount();

        long currentAdultsCount = order.getPassengers().stream()
                .filter(p -> p.getPassengerType() == PassengerType.ADULT).count();
        long currentChildrenCount = order.getPassengers().stream()
                .filter(p -> p.getPassengerType() == PassengerType.CHILD).count();

        if (currentAdultsCount != adultsCountRequired || currentChildrenCount != childrenCountRequired) {
            order.getPassengers().clear();

            for (long i = 0; i < adultsCountRequired; i++) {
                OrderPassenger adultPassenger = new OrderPassenger();
                adultPassenger.setOrder(order);
                adultPassenger.setPassengerType(PassengerType.ADULT);
                order.getPassengers().add(adultPassenger);
            }

            for (long i = 0; i < childrenCountRequired; i++) {
                OrderPassenger childPassenger = new OrderPassenger();
                childPassenger.setOrder(order);
                childPassenger.setPassengerType(PassengerType.CHILD);
                order.getPassengers().add(childPassenger);
            }
        }
    }

    @Transactional
    public PrepareCheckoutResponseDto saveServicesAndPrepareCheckout(Long orderId, ServicesFormDto form) {
        BookingOrder order = bookingOrderRepository.findByIdWithPassengers(orderId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Заказ (%d) не найден", orderId)));

        if (form.getSelectedServiceIds() != null && !form.getSelectedServiceIds().isEmpty()) {
            List<AdditionalService> services = additionalServiceRepository.findAllById(form.getSelectedServiceIds());
            order.setSelectedServices(new ArrayList<>(services));
        } else {
            order.setSelectedServices(new ArrayList<>());
        }

        List<OrderPassenger> passengers = order.getPassengers();
        BigDecimal seatsSurcharge = BigDecimal.ZERO;

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

                            if (sa.getSeat().getSeatClass() == SeatClass.EMERGENCY) {
                                seatsSurcharge = seatsSurcharge.add(BigDecimal.valueOf(400));
                            }
                        }
                    }
                }
            }
        }

        BigDecimal servicesTotal = order.getSelectedServices().stream()
                .map(AdditionalService::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = order.getBasePrice()
                .add(seatsSurcharge)
                .add(servicesTotal);

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            FlightInventory inventory = flightInventoryRepository
                    .findByFlightIdAndSeatClass(order.getOutboundFlight().getId(), order.getSeatClass())
                    .orElseThrow(() -> new IllegalArgumentException("FlightInventory not found"));
            inventory.setAvailableSeats(inventory.getAvailableSeats() - order.getPassengers().size());

            flightInventoryRepository.save(inventory);
        }

        order.setTotalPrice(total);
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        bookingOrderRepository.save(order);
        return new PrepareCheckoutResponseDto(seatsSurcharge, servicesTotal);
    }

    @Transactional
    public void processPayment(Long orderId, PaymentFormDto paymentFormDto) {
        BookingOrder bookingOrder = bookingOrderRepository.findByIdWithPassengersAndAccount(orderId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Заказ (%d) не найден", orderId)));

        List<Long> seatIds = bookingOrder.getPassengers().stream()
                .map(OrderPassenger::getOutboundSeatAvailability)
                .filter(Objects::nonNull)
                .map(SeatAvailability::getId)
                .toList();

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

        if (paymentFormDto.isPayWithMiles() && loyaltyAccount.getMiles() > 0) {
            int milesSpent = loyaltyAccount.getMiles();
            BigDecimal milesDiscount = BigDecimal.valueOf(milesSpent);
            BigDecimal newTotalPrice = bookingOrder.getTotalPrice().subtract(milesDiscount).max(BigDecimal.ZERO);

            bookingOrder.setMilesSpent(milesSpent);
            bookingOrder.setTotalPrice(newTotalPrice);
            loyaltyAccount.setMiles(0);

            LoyaltyTransaction spentTransaction = new LoyaltyTransaction();
            spentTransaction.setLoyaltyAccount(loyaltyAccount);
            spentTransaction.setOrder(bookingOrder);
            spentTransaction.setTransactionType(LoyaltyTransactionType.SPENT);
            spentTransaction.setMiles(milesSpent);
            spentTransaction.setCreatedAt(now);
            loyaltyAccount.getTransactions().add(spentTransaction);
        }

        int milesEarned = bookingOrder.getTotalPrice()
                .multiply(BigDecimal.valueOf(bookingOrder.getSeatClass().getMilesPercent() / 100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        bookingOrder.setMilesEarned(milesEarned);

        if (milesEarned > 0) {
            loyaltyAccount.setMiles(loyaltyAccount.getMiles() + milesEarned);

            LoyaltyTransaction earnedTransaction = new LoyaltyTransaction();
            earnedTransaction.setLoyaltyAccount(loyaltyAccount);
            earnedTransaction.setOrder(bookingOrder);
            earnedTransaction.setTransactionType(LoyaltyTransactionType.EARNED);
            earnedTransaction.setMiles(milesEarned);
            earnedTransaction.setCreatedAt(now);
            loyaltyAccount.getTransactions().add(earnedTransaction);
        }

        bookingOrder.setPaymentId(UUID.randomUUID().toString());
        bookingOrder.setStatus(OrderStatus.PAID);
        bookingOrderRepository.save(bookingOrder);

        log.info("Заказ {} успешно оплачен", bookingOrder.getId());
    }
}
