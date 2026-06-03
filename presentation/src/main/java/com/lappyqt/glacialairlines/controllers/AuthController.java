package com.lappyqt.glacialairlines.controllers;

import com.lappyqt.glacialairlines.exceptions.EmailAlreadyExistsException;
import com.lappyqt.glacialairlines.exceptions.PhoneAlreadyExistsException;
import com.lappyqt.glacialairlines.services.UserAccountService;
import dto.CreateAccountDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// Контроллер для управления процессами аутентификации пользователей и регистрации новых учетных записей
@Controller
@RequestMapping("/auth")
public class AuthController {
    private final UserAccountService userAccountService;

    public AuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    // Метод отображения страницы входа в систему (авторизации)
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // Метод отображения страницы регистрации нового пользователя авиакомпании
    @GetMapping("/create")
    public String createAccountPage(Model model) {
        // Инициализация пустого DTO-объекта формы регистрации
        model.addAttribute("createAccountDto", new CreateAccountDto());
        return "auth/create";
    }

    // Метод обработки и проверки данных формы регистрации нового аккаунта
    @PostMapping("/create")
    public String createAccount(@Valid CreateAccountDto createAccountDto,
                                BindingResult bindingResult,
                                Model model) {
        // Проверка корректности введенных данных пользователем
        if (bindingResult.hasErrors()) {
            return "auth/create";
        }

        try {
            // Передача данных на уровень бизнес-логики для создания пользователя
            userAccountService.createUserAccount(createAccountDto);
        }
        // Обработка бизнес-исключения: указанный email уже зарегистрирован в базе данных
        catch (EmailAlreadyExistsException exception) {
            bindingResult.rejectValue("email", "error.email", exception.getMessage());
            return "auth/create";
        }
        // Обработка бизнес-исключения: указанный номер телефона уже привязан к другому аккаунту
        catch (PhoneAlreadyExistsException exception) {
            bindingResult.rejectValue("phoneNumber", "error.phoneNumber", exception.getMessage());
            return "auth/create";
        }

        // Перенаправление на страницу логина после успешного создания учетной записи
        return "redirect:/auth/login";
    }
}
