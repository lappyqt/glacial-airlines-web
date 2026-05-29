package com.lappyqt.glacialairlines.services;

import com.lappyqt.glacialairlines.entities.account.UserAccount;
import com.lappyqt.glacialairlines.exceptions.EmailAlreadyExistsException;
import com.lappyqt.glacialairlines.exceptions.PhoneAlreadyExistsException;
import com.lappyqt.glacialairlines.repositories.account.UserAccountRepository;
import dto.CreateAccountDto;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountService {
    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createUserAccount(CreateAccountDto createAccountDto) {
        if (repository.existsByEmail(createAccountDto.getEmail())) {
            log.warn("Email уже занят: {}", createAccountDto.getEmail());
            throw new EmailAlreadyExistsException(createAccountDto.getEmail());
        }

        if (repository.existsByPhoneNumber(createAccountDto.getPhoneNumber())) {
            log.warn("Номер телефона уже занят: {}", createAccountDto.getPhoneNumber());
            throw new PhoneAlreadyExistsException(createAccountDto.getPhoneNumber());
        }

        UserAccount userAccount = UserAccount.builder()
                .lastName(StringUtils.capitalize(createAccountDto.getLastName().trim()))
                .firstName(StringUtils.capitalize(createAccountDto.getFirstName().trim()))
                .middleName(StringUtils.capitalize(createAccountDto.getMiddleName().trim()))
                .email(createAccountDto.getEmail().toLowerCase().trim())
                .phoneNumber(createAccountDto.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(createAccountDto.getPassword()))
                .emailVerified(false)
                .authProvider(com.lappyqt.glacialairlines.enums.AuthProvider.EMAIL)
                .build();

        repository.save(userAccount);
        log.info("Аккаунт успешно создан для email: {}", createAccountDto.getEmail());
    }

    public UserAccount findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Аккаунт с id %d не найден", id)));
    }
}
