package dto;

import com.lappyqt.glacialairlines.enums.DocumentType;
import com.lappyqt.glacialairlines.enums.Gender;
import dto.validation.FirstStep;
import dto.validation.SecondStep;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@GroupSequence({ CreatePassengerDto.class, FirstStep.class, SecondStep.class })
public class CreatePassengerDto {
    @NotBlank(message = "Имя не может быть пустым", groups = FirstStep.class)
    @Pattern(regexp = "^[\\p{L}'-]+$", message = "Имя — только буквы", groups = SecondStep.class)
    @Size(min = 3, max = 100, message = "Имя: длина 3-100 символов", groups = SecondStep.class)
    private String firstName;

    @NotBlank(message = "Фамилия не может быть пустой", groups = FirstStep.class)
    @Pattern(regexp = "^[\\p{L}'-]+$", message = "Фамилия — только буквы", groups = SecondStep.class)
    @Size(min = 3, max = 100, message = "Фамилия: длина 2-100 символов", groups = SecondStep.class)
    private String lastName;

    @Pattern(regexp = "^([\\p{L}'-]+)?$", message = "Отчество — только буквы")
    @Size(max = 100, message = "Отчество не может превышать 100 символов")
    private String middleName;

    @NotNull(message = "Пол не может быть пустым", groups = FirstStep.class)
    private Gender gender;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Дата рождения не может быть пустой", groups = FirstStep.class)
    @Past(message = "Дата рождения должна быть в прошлом", groups = SecondStep.class)
    private LocalDate birthDate;

    @NotNull(message = "Тип документа не может быть пустым", groups = FirstStep.class)
    private DocumentType documentType;

    @NotBlank(message = "Номер документа не может быть пустым", groups = FirstStep.class)
    @Size(min = 6, max = 50, message = "Номер документа: длина 6-50 символов", groups = SecondStep.class)
    @Pattern(regexp = "^[0-9]*$", message = "Номер — только цифры", groups = SecondStep.class)
    private String documentNumber;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат email")
    @Size(max = 150)
    private String contactEmail;

    @NotBlank(message = "Телефон не может быть пустым")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Неверный формат телефона")
    @Size(max = 20)
    private String contactPhone;
}
