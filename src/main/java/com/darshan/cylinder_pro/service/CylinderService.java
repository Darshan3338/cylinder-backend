package com.darshan.cylinder_pro.service;

import com.darshan.cylinder_pro.model.Booking;
import com.darshan.cylinder_pro.repo.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CylinderService {
@Autowired
private BookingRepository repo;

    public int calculateAverageDays(String deviceToken) {
        List<Booking> history = repo.findByDeviceTokenOrderByBookingDateAsc(deviceToken);

        if (history.size() < 2) return 30;

        LocalDate first = history.get(0).getBookingDate();
        LocalDate last = history.get(history.size() - 1).getBookingDate();

        long totalDays = ChronoUnit.DAYS.between(first, last);
        int intervals = history.size() - 1;

        if (totalDays <= 0) return 30;

        int calculatedAvg = (int) (totalDays / intervals);

        if (calculatedAvg < 20) return 20;
        if (calculatedAvg > 60) return 60;

        return calculatedAvg;
    }


    public String validateBooking(Booking newBooking) {
        LocalDate today = LocalDate.now();
        LocalDate bDate = newBooking.getBookingDate();

        if (bDate.isAfter(today)) return "Future dates are not allowed!";
        if (bDate.isBefore(today.minusDays(30))) return "Booking date is too old (max 30 days back).";

        List<Booking> history = repo.findByDeviceTokenOrderByBookingDateAsc(newBooking.getDeviceToken());

        for (Booking b : history) {
            long gap = Math.abs(ChronoUnit.DAYS.between(b.getBookingDate(), bDate));


            if (gap == 0) return "A booking already exists for this date!";


            if (gap < 30) {
                long wait = 30 - gap;
                return "Too soon! Please wait " + wait + " more days between refills.";
            }
        }
        return "OK";
    }

    public LocalDate predictNextDate(String deviceToken) {
        List<Booking> history = repo.findByDeviceTokenOrderByBookingDateAsc(deviceToken);


        if (history.isEmpty()) return LocalDate.now().plusDays(30);


        LocalDate lastBookingDate = history.get(history.size() - 1).getBookingDate();


        int avg = calculateAverageDays(deviceToken);

        return lastBookingDate.plusDays(avg);
    }
    public int getRemainingWaitDays(Booking newBooking) {

        List<Booking> history = repo.findByDeviceTokenOrderByBookingDateAsc(newBooking.getDeviceToken());


        if (history.isEmpty()) return 0;


        LocalDate lastDate = history.get(history.size() - 1).getBookingDate();


        long gap = ChronoUnit.DAYS.between(lastDate, newBooking.getBookingDate());

        if (gap >= 0 && gap < 30) {
            return (int) (30 - gap);
        }

        return 0;
    }
}

