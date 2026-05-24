package dto;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

interface FirstStep {}
interface SecondStep {}

@Data
@NoArgsConstructor
@GroupSequence({ FirstStep.class, SecondStep.class, CreateAccountDto.class })
public class CreateAccountDto {
    @NotBlank(message = "Фамилия не может быть пустой", groups = FirstStep.class)
    @Pattern(regexp = "^[\\p{L}'-]+$", message = "Фамилия — только буквы", groups = SecondStep.class)
    @Size(min = 3, max = 100, message = "Фамилия: длина 3-100 символов", groups = SecondStep.class)
    String lastName;

    @NotBlank(message = "Имя не может быть пустым", groups = FirstStep.class)
    @Pattern(regexp = "^[\\p{L}'-]+$", message = "Имя — только буквы", groups = SecondStep.class)
    @Size(min = 3, max = 100, message = "Имя: длина 3-100 символов", groups = SecondStep.class)
    String firstName;

    @Pattern(regexp = "^([\\p{L}'-]+)?$", message = "Отчество — только буквы")
    @Size(max = 100, message = "Отчество не может превышать 100 символов")
    String middleName;

    @NotBlank(message = "Email не может быть пустым", groups = FirstStep.class)
    @Email(message = "Неверный формат email", groups = SecondStep.class)
    @Size(max = 150, message = "Email не должен превышать 150 символов", groups = SecondStep.class)
    String email;

    @NotBlank(message = "Номер телефона не может быть пустым", groups = FirstStep.class)
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Неверный формат номера телефона", groups = SecondStep.class)
    @Size(max = 20, message = "Номер телефона не должен превышать 20 символов", groups = SecondStep.class)
    String phoneNumber;

    @NotBlank(message = "Пароль не может быть пустым", groups = FirstStep.class)
    @Size(min = 8, max = 72, message = "Пароль должен быть от 8 до 72 символов", groups = SecondStep.class)
    String password;
}
