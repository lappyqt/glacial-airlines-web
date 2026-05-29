package dto;

import com.lappyqt.glacialairlines.enums.DocumentType;
import com.lappyqt.glacialairlines.enums.Gender;
import com.lappyqt.glacialairlines.enums.PassengerType;
import dto.validation.FirstStep;
import dto.validation.SecondStep;
import dto.validation.ValidPassenger;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@ValidPassenger(groups = SecondStep.class)
@GroupSequence({ FirstStep.class, SecondStep.class, PassengerDto.class })
public class PassengerDto {
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

    @NotNull(message = "Дата рождения не может быть пустой", groups = FirstStep.class)
    @Past(message = "Дата рождения должна быть в прошлом", groups = SecondStep.class)
    private LocalDate birthDate;

    @NotNull(message = "Тип документа не может быть пустым", groups = FirstStep.class)
    private DocumentType documentType;

    @NotBlank(message = "Номер документа не может быть пустым", groups = FirstStep.class)
    @Size(min = 6, max = 50, message = "Номер документа: длина 6-50 символов", groups = SecondStep.class)
    private String documentNumber;

    @NotNull
    private PassengerType passengerType;
}
