package dto;

import com.lappyqt.glacialairlines.entities.flight.SeatAvailability;
import com.lappyqt.glacialairlines.enums.SeatClass;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class SeatGroupDto {
    private String title;
    private Map<Integer, List<SeatAvailability>> rows;

    public static String getGroupName(SeatClass seatClass) {
        return switch (seatClass) {
            case SeatClass.ECONOMY -> "Эконом";
            case SeatClass.BUSINESS -> "Бизнес";
            case SeatClass.EMERGENCY -> "Аварийный ряд";
        };
    }
}
