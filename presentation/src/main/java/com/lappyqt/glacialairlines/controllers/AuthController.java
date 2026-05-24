package com.lappyqt.glacialairlines.controllers;

import com.lappyqt.glacialairlines.exceptions.EmailAlreadyExistsException;
import com.lappyqt.glacialairlines.exceptions.PhoneAlreadyExistsException;
import com.lappyqt.glacialairlines.services.AuthService;
import dto.CreateAccountDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/create")
    public String createAccountPage(Model model) {
        model.addAttribute("createAccountDto", new CreateAccountDto());
        return "auth/create";
    }

    @PostMapping("/create")
    public String createAccount(@Valid CreateAccountDto createAccountDto,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/create";
        }

        try {
            authService.createUserAccount(createAccountDto);
        } catch (EmailAlreadyExistsException exception) {
            bindingResult.rejectValue("email", "error.email", exception.getMessage());
            return "auth/create";
        } catch (PhoneAlreadyExistsException exception) {
            bindingResult.rejectValue("phoneNumber", "error.phoneNumber", exception.getMessage());
            return "auth/create";
        }

        return "redirect:/auth/login";
    }
}
