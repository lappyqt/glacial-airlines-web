package com.lappyqt.glacialairlines.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/account")
public class AccountController {

    @GetMapping
    public String accountPageDefault(Model model) {
        return accountPage("orders", model);
    }

    @GetMapping(value = {"/", "/{tab}"})
    public String accountPage(@PathVariable(required = false, name = "tab") String tab, Model model) {
        String activeTab = (tab != null && !tab.isEmpty()) ? tab : "orders";
        model.addAttribute("activeTab", activeTab);
        return "/account/main";
    }
}
