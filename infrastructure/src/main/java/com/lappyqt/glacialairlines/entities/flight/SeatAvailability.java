package com.lappyqt.glacialairlines.entities.flight;

import com.lappyqt.glacialairlines.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "seat_availability",
        schema = "flight",
        uniqueConstraints = @UniqueConstraint(columnNames = {"flight_id", "seat_id"}))
@Getter @Setter
public class SeatAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seat_availability_seq")
    @SequenceGenerator(name = "seat_availability_seq", sequenceName = "seat_availability_id_seq")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "flight_id")
    private Flight flight;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status;
}
