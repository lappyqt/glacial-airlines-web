package com.lappyqt.glacialairlines.services.security;

import com.lappyqt.glacialairlines.entities.account.UserAccount;
import com.lappyqt.glacialairlines.repositories.account.UserAccountRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserAccountRepository userAccountRepository;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        UserAccount userAccount = userAccountRepository.findByEmailOrPhoneNumber(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        return new CustomUserDetails(
            userAccount.getId(),
            userAccount.getPhoneNumber(),
            userAccount.getEmail(),
            userAccount.getFirstName(),
            userAccount.getPasswordHash()
        );
    }
}
