package com.lappyqt.glacialairlines.entities.flight;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "aircraft", schema = "flight")
@Getter @Setter
public class Aircraft {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "aircraft_seq")
    @SequenceGenerator(name = "aircraft_seq", sequenceName = "aircraft_id_seq")
    private Long id;

    @Column(name = "registration_number", nullable = false, unique = true, length = 10)
    private String registrationNumber;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "economy_seats", nullable = false)
    private Integer economySeats;

    @Column(name = "emergency_seats", nullable = false)
    private Integer emergencySeats;

    @Column(name = "business_seats", nullable = false)
    private Integer businessSeats;
}
