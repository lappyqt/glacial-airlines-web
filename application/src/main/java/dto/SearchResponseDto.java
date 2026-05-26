package dto;

import com.lappyqt.glacialairlines.enums.SeatClass;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Data
@Builder
public class SearchResponseDto {
    private Long flightId;
    private String flightNumber;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Duration flightDuration;
    private String aircraftModel;

    private String departureIataCode;
    private String arrivalIataCode;

    private SeatClass seatClass;
    private Integer availableSeats;
    private BigDecimal pricePerAdult;
    private BigDecimal pricePerChild;
    private BigDecimal totalPrice;

    private BigDecimal milesEarned;

    @Nullable
    private BigDecimal priceWithMiles;
}
