package com.lappyqt.glacialairlines.services;

import com.lappyqt.glacialairlines.entities.account.UserAccount;
import com.lappyqt.glacialairlines.entities.booking.BookingOrder;
import com.lappyqt.glacialairlines.entities.booking.OrderPassenger;
import com.lappyqt.glacialairlines.enums.OrderStatus;
import com.lappyqt.glacialairlines.enums.PassengerType;
import com.lappyqt.glacialairlines.repositories.booking.BookingOrderRepository;
import com.lappyqt.glacialairlines.repositories.flight.FlightRepository;
import dto.PassengerDto;
import dto.PassengersFormDto;
import dto.SearchRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final FlightRepository flightRepository;
    private final BookingOrderRepository bookingOrderRepository;

    @Transactional(readOnly = true)
    public BookingOrder getOrder(Long orderId) {
        return bookingOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Заказ (%d) не найден", orderId)));
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
    public void savePassengers(Long orderId, PassengersFormDto passengersFormDto) {
        BookingOrder order = bookingOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Заказ (%d) не найден", orderId)));

        order.setContactEmail(passengersFormDto.getContactEmail().toLowerCase().trim());
        order.setContactPhone(passengersFormDto.getContactPhone().trim());

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
}
