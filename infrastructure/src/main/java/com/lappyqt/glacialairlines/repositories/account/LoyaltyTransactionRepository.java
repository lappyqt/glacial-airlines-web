package com.lappyqt.glacialairlines.repositories.account;

import com.lappyqt.glacialairlines.entities.account.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {
    @Query("""
        SELECT lt FROM LoyaltyTransaction lt
        JOIN FETCH lt.order
        WHERE lt.loyaltyAccount.id = :accountId
        ORDER BY lt.id DESC
    """)
    List<LoyaltyTransaction> findByLoyaltyAccountIdWithOrder(@Param("accountId") Long accountId);
}
