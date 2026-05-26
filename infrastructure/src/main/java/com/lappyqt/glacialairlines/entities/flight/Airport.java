package com.lappyqt.glacialairlines.entities.flight;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "airport", schema = "flight")
@Getter @Setter
public class Airport {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "airport_seq")
    @SequenceGenerator(name = "airport_seq", sequenceName = "airport_id_seq")
    private Long id;

    @Column(name = "iata_code", nullable = false, unique = true, length = 3)
    private String iataCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 150)
    private String city;

    @Column(nullable = false, length = 150)
    private String country;

    @Column(name = "offset_utc", nullable = false)
    private Integer offsetUTC;
}
