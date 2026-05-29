package dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class PassengersFormDto {
    @Valid
    @NotEmpty
    private List<PassengerDto> passengers;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат email")
    @Size(max = 150)
    private String contactEmail;

    @NotBlank(message = "Телефон не может быть пустым")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Неверный формат телефона")
    @Size(max = 20)
    private String contactPhone;
}
