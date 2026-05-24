package com.lappyqt.glacialairlines.entities.account;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loyalty_account", schema = "account")
@Getter
@Setter
@NoArgsConstructor
public class LoyaltyAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loyalty_account_seq")
    @SequenceGenerator(name = "loyalty_account_seq", sequenceName = "loyalty_account_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private int miles = 0;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private Instant createdAt;

    @OneToMany(mappedBy = "loyaltyAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoyaltyTransaction> transactions = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        createdAt = Instant.now();
    }
}
