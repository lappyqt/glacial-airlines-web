package com.lappyqt.glacialairlines.repositories.account;

import com.lappyqt.glacialairlines.entities.account.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<UserAccount> findByEmailOrPhoneNumber(String email, String phoneNumber);

    @Query("SELECT u.loyaltyAccount.miles FROM UserAccount u WHERE u.id = :id")
    Optional<Integer> findMilesByUserId(@Param("id") Long id);
}
