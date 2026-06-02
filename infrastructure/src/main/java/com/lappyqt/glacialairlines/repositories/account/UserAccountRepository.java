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

    @Query("""
        SELECT u FROM UserAccount u
        LEFT JOIN FETCH u.passenger
        WHERE u.id = :id
        """)
    Optional<UserAccount> findByIdWithPassenger(@Param("id") Long userId);

    @Query("""
        SELECT u FROM UserAccount u
        LEFT JOIN FETCH u.loyaltyAccount la
        LEFT JOIN FETCH u.passenger
        LEFT JOIN FETCH la.transactions t
        LEFT JOIN FETCH t.order o
        WHERE u.id = :id
        ORDER BY t.id DESC
        """)
    Optional<UserAccount> findByIdWithTransactions(@Param("id") Long userId);
}
