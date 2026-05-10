package com.lappyqt.glacialairlines.entities.booking;

import com.lappyqt.glacialairlines.entities.account.Passenger;
import com.lappyqt.glacialairlines.entities.flight.Seat;
import com.lappyqt.glacialairlines.enums.DocumentType;
import com.lappyqt.glacialairlines.enums.Gender;
import com.lappyqt.glacialairlines.enums.PassengerType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "order_passenger", schema = "booking")
@Getter @Setter
public class OrderPassenger {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_passenger_seq")
    @SequenceGenerator(name = "order_passenger_seq", sequenceName = "order_passenger_id_seq")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private BookingOrder order;

    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private Passenger passengerProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_type", nullable = false, length = 10)
    private PassengerType passengerType;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false, length = 50)
    private String documentNumber;

    @ManyToOne
    @JoinColumn(name = "outbound_seat_id")
    private Seat outboundSeat;
}
