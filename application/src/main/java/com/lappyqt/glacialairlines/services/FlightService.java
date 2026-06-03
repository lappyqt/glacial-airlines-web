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

// Сервисный класс для управления данными авиарейсов, поиска предложений и работы со схемой мест
@Service
@RequiredArgsConstructor
public class FlightService {
    private final AirportRepository airportRepository;
    private final FlightInventoryRepository flightInventoryRepository;
    private final SeatAvailabilityRepository seatAvailabilityRepository;

    // Метод для получения полного списка аэропортов
    @Transactional(readOnly = true)
    public List<Airport> getAirportList() {
        return airportRepository.findAll();
    }

    // Метод для поиска доступных вариантов перелета "туда" с фильтрацией и сортировкой
    @Transactional(readOnly = true)
    public List<SearchResponseDto> findAvailableFlightOffers(SearchRequestDto searchRequestDto, int milesCount, String filter) {
        // Подсчет количества пассажиров
        int adultsCount = searchRequestDto.getAdultsCount();
        int childrenCount = searchRequestDto.getChildrenCount();
        int totalPassengerCount = adultsCount + childrenCount;

        // Получение компаратора для сортировки результатов
        Comparator<SearchResponseDto> comparator = this.getComparatorForFlightOffers(filter);

        // Валидация введенных дат и лимитов
        compareFlightDates(searchRequestDto.getOutboundFlightDate(), searchRequestDto.getReturnFlightDate());
        checkPassengerLimit(totalPassengerCount);

        // Запрос к репозиторию, маппинг сущностей в DTO и сортировка
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

    // Метод получения информации о конкретном рейсе по его идентификатору
    @Transactional(readOnly = true)
    public SearchResponseDto getFlightOffer(Long flightId, SearchRequestDto searchRequestDto) {
        // Поиск инвентарных данных рейса или генерация исключения, если рейс не найден
        FlightInventory flightInventory = flightInventoryRepository
                .findByFlightIdAndSeatClass(flightId, searchRequestDto.getServiceClass())
                .orElseThrow(() -> new IllegalArgumentException(String.format("FlightInventory c flight_id (%d) не найден", flightId)));
        return mapFlightInventoryToDto(flightInventory, 0, searchRequestDto.getAdultsCount(), searchRequestDto.getChildrenCount());
    }

    // Метод для поиска доступных вариантов перелета "обратно"
    @Transactional(readOnly = true)
    public List<SearchResponseDto> findAvailableReturnFlightOffers(SearchRequestDto searchRequestDto, int milesCount, String filter) {
        int adultsCount = searchRequestDto.getAdultsCount();
        int childrenCount = searchRequestDto.getChildrenCount();

        // Определение правила сортировки обратных рейсов
        Comparator<SearchResponseDto> comparator = this.getComparatorForFlightOffers(filter);

        // Поиск рейсов в обратном направлении (смена местами аэропортов вылета и прилета)
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

    // Метод получения и группировки посадочных мест рейса по рядам и классам обслуживания
    @Transactional(readOnly = true)
    public List<SeatGroupDto> getSeatGroups(Long flightId) {
        List<SeatAvailability> seatAvailabilityList = seatAvailabilityRepository.findByFlightIdAndSeatClass(flightId);

        if (seatAvailabilityList.isEmpty()) {
            throw new IllegalArgumentException(String.format("Список доступных мест для рейса %d не найден", flightId));
        }

        // Группировка мест по номеру ряда с сохранением порядка (TreeMap)
        Map<Integer, List<SeatAvailability>> seatsByRow = seatAvailabilityList.stream()
                .collect(Collectors.groupingBy(sa -> sa.getSeat().getRowNumber(), TreeMap::new, Collectors.toList()));

        // Формирование иерархической структуры групп мест для интерфейса
        List<SeatGroupDto> seatGroups = new ArrayList<>();
        SeatGroupDto currentGroup = null;
        SeatClass previousClass = null;

        for (Map.Entry<Integer, List<SeatAvailability>> entry : seatsByRow.entrySet()) {
            Integer rowNumber = entry.getKey();
            List<SeatAvailability> rowSeats = entry.getValue();

            // Извлечение класса обслуживания для текущего ряда
            SeatClass currentClass = rowSeats.getFirst().getSeat().getSeatClass();

            // Создание новой группы мест при смене класса (например, переход от Бизнеса к Эконому)
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

    // Вспомогательный метод для выбора правила сортировки (компаратора) на основе переданного фильтра
    private Comparator<SearchResponseDto> getComparatorForFlightOffers(String filter) {
        return switch (filter) {
            case "duration" -> Comparator.comparing(SearchResponseDto::getFlightDuration);
            case "departureTime" -> Comparator.comparing(SearchResponseDto::getDepartureTime);
            case "arrivalTime" -> Comparator.comparing(SearchResponseDto::getArrivalTime);
            default -> Comparator.comparing(SearchResponseDto::getTotalPrice);
        };
    }

    // Вспомогательный метод для валидации дат (проверка, что дата вылета не позже даты возвращения)
    private void compareFlightDates(LocalDate outboundDate, LocalDate returnDate) throws OutboundDateAfterReturnDateException {
        if (returnDate == null) return;;

        if (outboundDate.isAfter(returnDate)) {
            throw new OutboundDateAfterReturnDateException(outboundDate, returnDate);
        }
    }

    // Вспомогательный метод контроля максимального количества пассажиров в одном заказе (не более 9)
    private void checkPassengerLimit(Integer totalCount) throws PassengerLimitExceededException {
        if (totalCount > 9) throw new PassengerLimitExceededException(totalCount);
    }

    // Вспомогательный метод для маппинга сущности FlightInventory в DTO с расчетом стоимости, скидок и миль
    private SearchResponseDto mapFlightInventoryToDto(FlightInventory flightInventory, int milesCount, int adultCount, int childrenCount) {
        Flight flight = flightInventory.getFlight();
        Route route = flight.getRoute();

        // Расчет стоимости для взрослых, детей и общей итоговой стоимости
        BigDecimal adultPrice = flightInventory.getPrice();
        BigDecimal childPrice = adultPrice.multiply(flightInventory.getChildSeatDiscount());
        BigDecimal totalPrice = adultPrice.multiply(BigDecimal.valueOf(adultCount)).add(childPrice.multiply(BigDecimal.valueOf(childrenCount)));

        // Расчет количества начисляемых за полет миль
        BigDecimal milesEarned = totalPrice.multiply(
            BigDecimal.valueOf(flightInventory.getSeatClass().getMilesPercent() / 100)
        ).setScale(0, RoundingMode.HALF_UP);

        // Расчет финальной стоимости с учетом списания накопленных миль пользователя
        BigDecimal priceWithMiles = totalPrice.subtract(BigDecimal.valueOf(milesCount)).max(BigDecimal.ZERO);

        // Корректировка времени вылета и прилета с учетом временных зон (UTC)
        LocalDateTime departureUTC = flight.getDepartureTime()
                .minusHours(route.getDepartureAirport().getOffsetUTC());

        LocalDateTime arrivalUTC = flight.getArrivalTime()
                .minusHours(route.getArrivalAirport().getOffsetUTC());

        // Вычисление "чистой" продолжительности полета
        Duration flightDuration = Duration.between(departureUTC, arrivalUTC);

        // Сборка и возвращение готового DTO объекта
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
