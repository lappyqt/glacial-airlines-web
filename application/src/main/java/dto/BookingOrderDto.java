package dto;

import com.lappyqt.glacialairlines.entities.booking.AdditionalService;
import com.lappyqt.glacialairlines.entities.booking.BookingOrder;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BookingOrderDto {
    private BookingOrder bookingOrder;
    private SearchResponseDto outboundFlight;
    private SearchResponseDto returnFlight;
    private List<AdditionalService> availableServices;
    private boolean isEditable;
    private boolean refundAvailable;
}
