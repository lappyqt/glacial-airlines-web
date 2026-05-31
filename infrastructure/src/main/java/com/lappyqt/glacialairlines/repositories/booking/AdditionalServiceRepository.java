package com.lappyqt.glacialairlines.repositories.booking;

import com.lappyqt.glacialairlines.entities.booking.AdditionalService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdditionalServiceRepository extends JpaRepository<AdditionalService, Long> {
    List<AdditionalService> findByIsActiveTrue();
}
