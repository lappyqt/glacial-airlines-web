package com.lappyqt.glacialairlines.entities.booking;

import com.lappyqt.glacialairlines.enums.AdditionalServiceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "additional_service", schema = "booking")
@Getter @Setter
public class AdditionalService {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "additional_service_seq")
    @SequenceGenerator(name = "additional_service_seq", sequenceName = "additional_service_id_seq", allocationSize = 1)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "additional_service_type", unique = true, nullable = false, length = 50)
    private AdditionalServiceType serviceType;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "is_active",nullable = false)
    private Boolean isActive = false;
}
