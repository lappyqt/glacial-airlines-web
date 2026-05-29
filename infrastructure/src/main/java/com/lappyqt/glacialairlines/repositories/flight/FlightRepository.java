package com.lappyqt.glacialairlines.repositories.flight;

import com.lappyqt.glacialairlines.entities.flight.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightRepository extends JpaRepository<Flight, Long> {}
