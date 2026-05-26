package com.lappyqt.glacialairlines.services;

import com.lappyqt.glacialairlines.entities.flight.Airport;
import com.lappyqt.glacialairlines.entities.flight.Flight;
import com.lappyqt.glacialairlines.entities.flight.FlightInventory;
import com.lappyqt.glacialairlines.entities.flight.Route;
import com.lappyqt.glacialairlines.enums.FlightStatus;
import com.lappyqt.glacialairlines.exceptions.OutboundDateAfterReturnDateException;
import com.lappyqt.glacialairlines.exceptions.PassengerLimitExceededException;
import com.lappyqt.glacialairlines.repositories.flight.AirportRepository;
import com.lappyqt.glacialairlines.repositories.flight.FlightInventoryRepository;
import dto.SearchRequestDto;
import dto.SearchResponseDto;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightService {
    private final AirportRepository airportRepository;
    private final FlightInventoryRepository flightInventoryRepository;

    @Transactional(readOnly = true)
    public List<Airport> getAirportList() {
        return airportRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<SearchResponseDto> findAvailableFlightOffers(SearchRequestDto searchRequestDto) {
        int adultsCount = searchRequestDto.getAdultsCount();
        int childrenCount = searchRequestDto.getChildrenCount();

        compareFlightDates(searchRequestDto.getOutboundFlightDate(), searchRequestDto.getReturnFlightDate());
        checkPassengerLimit(adultsCount + childrenCount);

        return flightInventoryRepository.findAvailable(
                searchRequestDto.getOutboundAirportId(),
                searchRequestDto.getReturnAirportId(),
                searchRequestDto.getServiceClass(),
                searchRequestDto.getAdultsCount() + searchRequestDto.getChildrenCount(),
                FlightStatus.SCHEDULED,
                searchRequestDto.getOutboundFlightDate()
        ).stream().map(flightInventory -> availableFlightsToDto(flightInventory, adultsCount, childrenCount)).toList();
    }

    private void compareFlightDates(LocalDate outboundDate, LocalDate returnDate) throws OutboundDateAfterReturnDateException {
        if (returnDate == null) return;;

        if (outboundDate.isAfter(returnDate)) {
            throw new OutboundDateAfterReturnDateException(outboundDate, returnDate);
        }
    }

    private void checkPassengerLimit(Integer totalCount) throws PassengerLimitExceededException {
        if (totalCount > 9) throw new PassengerLimitExceededException(totalCount);
    }

    private SearchResponseDto availableFlightsToDto(FlightInventory flightInventory, int adultCount, int childrenCount) {
        Flight flight = flightInventory.getFlight();
        Route route = flight.getRoute();

        BigDecimal adultPrice = flightInventory.getPrice();
        BigDecimal childPrice = adultPrice.multiply(flightInventory.getChildSeatDiscount());
        BigDecimal totalPrice = adultPrice.multiply(BigDecimal.valueOf(adultCount)).add(childPrice.multiply(BigDecimal.valueOf(childrenCount)));

        BigDecimal milesEarned = totalPrice.multiply(
            BigDecimal.valueOf(flightInventory.getSeatClass().getMilesPercent() / 100)
        ).setScale(0, RoundingMode.HALF_UP);

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
                .seatClass(flightInventory.getSeatClass())
                .availableSeats(flightInventory.getAvailableSeats())
                .pricePerAdult(adultPrice)
                .pricePerChild(childPrice)
                .totalPrice(totalPrice)
                .milesEarned(milesEarned)
                .build();
    }
}
