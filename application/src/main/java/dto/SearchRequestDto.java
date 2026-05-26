package dto;

import com.lappyqt.glacialairlines.enums.SeatClass;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class SearchRequestDto {
    @NotNull(message = "Аэропорт отправления не может быть пустым")
    @Positive(message = "Номер аэропорта должен быть положительным")
    private Long outboundAirportId;

    @Positive(message = "Номер аэропорта прибытия должен быть положительным")
    private Long returnAirportId;

    @NotNull(message = "Дата вылета обязательна для заполнения")
    @FutureOrPresent(message = "Дата не может быть в прошлом")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate outboundFlightDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate returnFlightDate;

    @Min(value = 1, message = "Количество взрослых пассажиров от 1 и более")
    private Integer adultsCount = 1;

    @Min(0)
    private Integer childrenCount = 0;

    @NotNull(message = "Класс полёта является обязателеным")
    private SeatClass serviceClass = SeatClass.ECONOMY;

    private Boolean milesIncluded;
}
