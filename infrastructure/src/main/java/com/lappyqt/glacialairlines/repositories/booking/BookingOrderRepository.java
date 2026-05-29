package com.lappyqt.glacialairlines.repositories.booking;

import com.lappyqt.glacialairlines.entities.booking.BookingOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingOrderRepository extends JpaRepository<BookingOrder, Long> {}
