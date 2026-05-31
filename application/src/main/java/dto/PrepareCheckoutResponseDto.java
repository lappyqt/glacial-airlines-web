package dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PrepareCheckoutResponseDto {
    private BigDecimal seatsSurcharge;
    private BigDecimal servicesTotal;
}
