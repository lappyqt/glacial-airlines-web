package com.lappyqt.glacialairlines.entities.flight;

import com.lappyqt.glacialairlines.enums.SeatClass;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "flight_inventory", schema = "flight",
        uniqueConstraints = @UniqueConstraint(columnNames = {"flight_id", "seat_class"}))
@Getter @Setter
public class FlightInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flight_inventory_seq")
    @SequenceGenerator(name = "flight_inventory_seq", sequenceName = "flight_inventory_id_seq")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "flight_id")
    private Flight flight;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false, length = 20)
    private SeatClass seatClass;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "child_seat_discount", nullable = false, precision = 4, scale = 2)
    private BigDecimal childSeatDiscount;
}
