package com.lappyqt.glacialairlines.entities.account;

import com.lappyqt.glacialairlines.entities.booking.BookingOrder;
import com.lappyqt.glacialairlines.enums.LoyaltyTransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "loyalty_transaction", schema = "account")
@Getter @Setter
public class LoyaltyTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loyalty_transaction_seq")
    @SequenceGenerator(name = "loyalty_transaction_seq", sequenceName = "loyalty_transaction_seq")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "loyalty_account_id")
    private LoyaltyAccount loyaltyAccount;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private BookingOrder order;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private LoyaltyTransactionType transactionType;

    @Column(nullable = false)
    private Integer miles;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private Instant createdAt;
}
