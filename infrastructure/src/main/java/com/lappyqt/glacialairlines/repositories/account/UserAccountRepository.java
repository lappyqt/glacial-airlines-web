package com.lappyqt.glacialairlines.repositories.account;

import com.lappyqt.glacialairlines.entities.account.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<UserAccount> findByEmailOrPhoneNumber(String email, String phoneNumber);
}
