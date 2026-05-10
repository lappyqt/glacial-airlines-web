package com.lappyqt.glacialairlines.entities.flight;

import com.lappyqt.glacialairlines.enums.SeatClass;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "seat",
        schema = "flight",
        uniqueConstraints = @UniqueConstraint(columnNames = {"aircraft_id", "seat_number"}))
@Getter @Setter
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seat_seq")
    @SequenceGenerator(name = "seat_seq", sequenceName = "seat_id_seq")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aircraft_id")
    private Aircraft aircraft;

    @Column(name = "seat_number", nullable = false, length = 5)
    private String seatNumber;

    @Column(name = "seat_letter", nullable = false, length = 1)
    private String seatLetter;

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false, length = 20)
    private SeatClass seatClass;
}
