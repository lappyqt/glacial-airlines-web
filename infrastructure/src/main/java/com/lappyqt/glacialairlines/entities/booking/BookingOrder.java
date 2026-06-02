package com.lappyqt.glacialairlines.entities.booking;

import com.lappyqt.glacialairlines.entities.account.LoyaltyTransaction;
import com.lappyqt.glacialairlines.entities.account.UserAccount;
import com.lappyqt.glacialairlines.entities.flight.Flight;
import com.lappyqt.glacialairlines.enums.OrderStatus;
import com.lappyqt.glacialairlines.enums.SeatClass;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "booking_order", schema = "booking")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_order_seq")
    @SequenceGenerator(name = "booking_order_seq", sequenceName = "booking_order_id_seq")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_account_id")
    private UserAccount userAccount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "outbound_flight_id")
    private Flight outboundFlight;

    @ManyToOne
    @JoinColumn(name = "return_flight_id")
    private Flight returnFlight;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false, length = 20)
    private SeatClass seatClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "miles_spent", nullable = false)
    @Builder.Default
    private Integer milesSpent = 0;

    @Column(name = "miles_earned", nullable = false)
    @Builder.Default
    private Integer milesEarned = 0;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "payment_id", length = 100)
    private String paymentId;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private Instant createdAt;

    @Column(name = "booking_expires_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private Instant bookingExpiresAt;

    @Column(name = "returned_at", columnDefinition = "timestamptz")
    private Instant returnedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderPassenger> passengers = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LoyaltyTransaction> loyaltyTransactions = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "order_services",
            schema = "booking",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    @Builder.Default
    private List<AdditionalService> selectedServices = new ArrayList<>();
}
