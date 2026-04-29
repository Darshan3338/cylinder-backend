package com.darshan.cylinder_pro.repo;

import com.darshan.cylinder_pro.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking,Long> {
    List<Booking> findByDeviceTokenOrderByBookingDateAsc(String deviceToken);
    @Query("SELECT DISTINCT b.deviceToken FROM Booking b")
    List<String> findAllUniqueDeviceTokens();
}
