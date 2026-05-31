package com.lappyqt.glacialairlines.controllers.advices;

import com.lappyqt.glacialairlines.repositories.account.UserAccountRepository;
import com.lappyqt.glacialairlines.services.security.CustomUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@AllArgsConstructor
public class GlobalModelAdvice {
    private final UserAccountRepository repository;

    @ModelAttribute("currentMiles")
    public int addMilesToModel(@AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) return 0;
        return repository.findMilesByUserId(currentUser.getId()).orElse(0);
    }
}
