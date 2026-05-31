package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PaymentFormDto {
    @NotBlank(message = "Укажите номер карты")
    @Pattern(regexp = "\\d{16}", message = "Номер карты должен содержать 16 цифр")
    private String cardNumber;

    @NotBlank(message = "Укажите срок действия")
    @Pattern(regexp = "\\d{2}/\\d{2}", message = "Формат: MM/YY")
    private String validityPeriod;

    @NotBlank(message = "Укажите CVV")
    @Pattern(regexp = "\\d{3}", message = "CVV должен содержать 3 цифры")
    private String cvv;

    @NotBlank(message = "Укажите имя держателя карты")
    private String cardholder;

    private boolean payWithMiles;
}
