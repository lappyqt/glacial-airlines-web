package dto.validation;

import com.lappyqt.glacialairlines.enums.DocumentType;
import com.lappyqt.glacialairlines.enums.PassengerType;
import dto.PassengerDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class ValidPassengerValidator implements ConstraintValidator<ValidPassenger, PassengerDto> {
    @Override
    public boolean isValid(PassengerDto passengerDto, ConstraintValidatorContext context) {
        if (passengerDto.getBirthDate() == null || passengerDto.getPassengerType() == null) return true;

        boolean valid = true;
        LocalDate now = LocalDate.now();
        int age = Period.between(passengerDto.getBirthDate(), now).getYears();

        // Проверяем возраст пассажира
        if (passengerDto.getPassengerType() == PassengerType.CHILD && age >= 12) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Ребёнок должен быть младше 12 лет")
                    .addPropertyNode("birthDate")
                    .addConstraintViolation();
            valid = false;
        } else if (passengerDto.getPassengerType() == PassengerType.ADULT && age < 12) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Взрослый пассажир должен быть не моложе 12 лет")
                    .addPropertyNode("birthDate")
                    .addConstraintViolation();
            valid = false;
        } else if (age > 120) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Некорректная дата рождения")
                    .addPropertyNode("birthDate")
                    .addConstraintViolation();
            valid = false;
        }

        // Проверяем соотвествие типа документа и типа пассадира
        if (passengerDto.getDocumentType() != null) {
            if (passengerDto.getPassengerType() == PassengerType.CHILD &&
                    passengerDto.getDocumentType() != DocumentType.BIRTH_CERTIFICATE) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Для ребёнка необходимо указать свидетельство о рождении")
                        .addPropertyNode("documentNumber")
                        .addConstraintViolation();
                valid = false;
            }

            if (passengerDto.getPassengerType() == PassengerType.ADULT &&
                    passengerDto.getDocumentType() == DocumentType.BIRTH_CERTIFICATE) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Свидетельство о рождении недоступно для взрослых")
                        .addPropertyNode("documentNumber")
                        .addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}
