package dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ServicesFormDto {
    private List<Long> selectedServiceIds = new ArrayList<>();
    private List<Long> outboundSeatIds = new ArrayList<>();
    private boolean skipSeats;
}