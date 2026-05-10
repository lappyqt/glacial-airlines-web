package com.lappyqt.glacialairlines.entities.flight;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "route",
        schema = "flight",
        uniqueConstraints = @UniqueConstraint(columnNames = {"departure_airport_id", "arrival_airport_id"}))
@Getter @Setter
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "route_seq")
    @SequenceGenerator(name = "route_seq", sequenceName = "route_id_seq")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "departure_airport_id")
    private Airport departureAirport;

    @ManyToOne(optional = false)
    @JoinColumn(name = "arrival_airport_id")
    private Airport arrivalAirport;
}
