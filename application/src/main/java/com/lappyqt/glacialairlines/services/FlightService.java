package com.lappyqt.glacialairlines.services;

import com.lappyqt.glacialairlines.entities.flight.*;
import com.lappyqt.glacialairlines.enums.FlightStatus;
import com.lappyqt.glacialairlines.enums.SeatClass;
import com.lappyqt.glacialairlines.exceptions.OutboundDateAfterReturnDateException;
import com.lappyqt.glacialairlines.exceptions.PassengerLimitExceededException;
import com.lappyqt.glacialairlines.repositories.flight.AirportRepository;
import com.lappyqt.glacialairlines.repositories.flight.FlightInventoryRepository;
import com.lappyqt.glacialairlines.repositories.flight.SeatAvailabilityRepository;
import dto.SearchRequestDto;
import dto.SearchResponseDto;
import dto.SeatGroupDto;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlightService {
    private final AirportRepository airportRepository;
    private final FlightInventoryRepository flightInventoryRepository;
    private final SeatAvailabilityRepository seatAvailabilityRepository;

    @Transactional(readOnly = true)
    public List<Airport> getAirportList() {
        return airportRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<SearchResponseDto> findAvailableFlightOffers(SearchRequestDto searchRequestDto, int milesCount, String filter) {
        int adultsCount = searchRequestDto.getAdultsCount();
        int childrenCount = searchRequestDto.getChildrenCount();
        int totalPassengerCount = adultsCount + childrenCount;

        Comparator<SearchResponseDto> comparator = this.getComparatorForFlightOffers(filter);

        compareFlightDates(searchRequestDto.getOutboundFlightDate(), searchRequestDto.getReturnFlightDate());
        checkPassengerLimit(totalPassengerCount);

        return flightInventoryRepository.findAvailable(
                searchRequestDto.getOutboundAirportId(),
                searchRequestDto.getReturnAirportId(),
                searchRequestDto.getServiceClass(),
                totalPassengerCount,
                FlightStatus.SCHEDULED,
                searchRequestDto.getOutboundFlightDate()
        ).stream().map(flightInventory -> mapFlightInventoryToDto(flightInventory, milesCount, adultsCount, childrenCount))
            .sorted(comparator).toList();
    }

    @Transactional(readOnly = true)
    public SearchResponseDto getFlightOffer(Long flightId, SearchRequestDto searchRequestDto) {
        FlightInventory flightInventory = flightInventoryRepository
                .findByFlightIdAndSeatClass(flightId, searchRequestDto.getServiceClass())
                .orElseThrow(() -> new IllegalArgumentException(String.format("FlightInventory c flight_id (%d) не найден", flightId)));
        return mapFlightInventoryToDto(flightInventory, 0, searchRequestDto.getAdultsCount(), searchRequestDto.getChildrenCount());
    }

    @Transactional(readOnly = true)
    public List<SearchResponseDto> findAvailableReturnFlightOffers(SearchRequestDto searchRequestDto, int milesCount, String filter) {
        int adultsCount = searchRequestDto.getAdultsCount();
        int childrenCount = searchRequestDto.getChildrenCount();

        Comparator<SearchResponseDto> comparator = this.getComparatorForFlightOffers(filter);

        return flightInventoryRepository.findAvailable(
                searchRequestDto.getReturnAirportId(),
                searchRequestDto.getOutboundAirportId(),
                searchRequestDto.getServiceClass(),
                adultsCount + childrenCount,
                FlightStatus.SCHEDULED,
                searchRequestDto.getReturnFlightDate()
        ).stream().map(flightInventory -> mapFlightInventoryToDto(flightInventory, milesCount, adultsCount, childrenCount))
                .sorted(comparator).toList();
    }

    @Transactional(readOnly = true)
    public List<SeatGroupDto> getSeatGroups(Long flightId) {
        List<SeatAvailability> seatAvailabilityList = seatAvailabilityRepository.findByFlightIdAndSeatClass(flightId);

        if (seatAvailabilityList.isEmpty()) {
            throw new IllegalArgumentException(String.format("Список доступных мест для рейса %d не найден", flightId));
        }

        // Формируем ряды
        Map<Integer, List<SeatAvailability>> seatsByRow = seatAvailabilityList.stream()
                .collect(Collectors.groupingBy(sa -> sa.getSeat().getRowNumber(), TreeMap::new, Collectors.toList()));

        // Формируем группы
        List<SeatGroupDto> seatGroups = new ArrayList<>();
        SeatGroupDto currentGroup = null;
        SeatClass previousClass = null;

        for (Map.Entry<Integer, List<SeatAvailability>> entry : seatsByRow.entrySet()) {
            Integer rowNumber = entry.getKey();
            List<SeatAvailability> rowSeats = entry.getValue();

            // Определяем класс текущего ряда
            SeatClass currentClass = rowSeats.getFirst().getSeat().getSeatClass();

            if (!currentClass.equals(previousClass)) {
                currentGroup = new SeatGroupDto(SeatGroupDto.getGroupName(currentClass), new LinkedHashMap<>());
                seatGroups.add(currentGroup);
            }

            // Добавляем ряд в текущую группу
            currentGroup.getRows().put(rowNumber, rowSeats);
            previousClass = currentClass;
        }

        return seatGroups;
    }

    private Comparator<SearchResponseDto> getComparatorForFlightOffers(String filter) {
        return switch (filter) {
            case "duration" -> Comparator.comparing(SearchResponseDto::getFlightDuration);
            case "departureTime" -> Comparator.comparing(SearchResponseDto::getDepartureTime);
            case "arrivalTime" -> Comparator.comparing(SearchResponseDto::getArrivalTime);
            default -> Comparator.comparing(SearchResponseDto::getTotalPrice);
        };
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

    private SearchResponseDto mapFlightInventoryToDto(FlightInventory flightInventory, int milesCount, int adultCount, int childrenCount) {
        Flight flight = flightInventory.getFlight();
        Route route = flight.getRoute();

        BigDecimal adultPrice = flightInventory.getPrice();
        BigDecimal childPrice = adultPrice.multiply(flightInventory.getChildSeatDiscount());
        BigDecimal totalPrice = adultPrice.multiply(BigDecimal.valueOf(adultCount)).add(childPrice.multiply(BigDecimal.valueOf(childrenCount)));

        BigDecimal milesEarned = totalPrice.multiply(
            BigDecimal.valueOf(flightInventory.getSeatClass().getMilesPercent() / 100)
        ).setScale(0, RoundingMode.HALF_UP);

        BigDecimal priceWithMiles = totalPrice.subtract(BigDecimal.valueOf(milesCount)).max(BigDecimal.ZERO);

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
                .pricePerAdult(adultPrice)
                .pricePerChild(childPrice)
                .priceWithMiles(priceWithMiles)
                .totalPrice(totalPrice)
                .milesEarned(milesEarned)
                .build();
    }
}
