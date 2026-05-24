package com.lappyqt.glacialairlines.repositories.flight;

import com.lappyqt.glacialairlines.entities.flight.Airport;
import com.lappyqt.glacialairlines.repositories.ReadOnlyRepository;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface AirportRepository extends ReadOnlyRepository<Airport, Long> {
    List<Airport> findAll();
    Optional<Airport> findById(Long id);
}
