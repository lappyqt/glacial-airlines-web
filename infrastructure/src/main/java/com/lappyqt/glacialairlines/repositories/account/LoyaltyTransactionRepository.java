package com.lappyqt.glacialairlines.repositories.account;

import com.lappyqt.glacialairlines.entities.account.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {}
