package com.darshan.cylinder_pro.Controller;

import com.darshan.cylinder_pro.model.Booking;
import com.darshan.cylinder_pro.repo.BookingRepository;
import com.darshan.cylinder_pro.service.CylinderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/gas")
@CrossOrigin(origins = {"https://wantednews.in",
        "https://www.wantednews.in",
        "http://localhost:5173"})
public class CylinderController {

@Autowired
private CylinderService cylinderService;

    @Autowired
    private BookingRepository repo;
    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/days-left/{deviceToken}")
    public ResponseEntity<Integer> getDaysRemaining(@PathVariable String deviceToken) {
        LocalDate nextDate = cylinderService.predictNextDate(deviceToken);
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), nextDate);
        return ResponseEntity.ok((int) daysLeft);
    }

    @PostMapping("/book")
    public ResponseEntity<?> addBooking(@Valid @RequestBody Booking booking) {
        if (booking.getBookingDate().isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest()
                    .body("Time travel detected! 🚀 You cannot log a future booking.");
        }


        int daysToWait = cylinderService.getRemainingWaitDays(booking);

        if (daysToWait > 0) {
            return ResponseEntity.badRequest()
                    .body("Too soon! ✋ Please wait " + daysToWait + " more days before logging another refill.");
        }
        Booking saved = repo.save(booking);
        return ResponseEntity.ok(saved);
    }
    @GetMapping("/history/{deviceToken}")
    public ResponseEntity<List<Booking>> getHistory(@PathVariable String deviceToken) {
        List<Booking> history = repo.findByDeviceTokenOrderByBookingDateAsc(deviceToken);
        return ResponseEntity.ok(history);
    }
    @GetMapping("/test-email")
    public String testEmail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("your-email@gmail.com");
            message.setTo("darshancmp356@gmail.com");
            message.setSubject("Cylinder Pro Test! 🔥");
            message.setText("The notification system is officially connected!");

            mailSender.send(message);
            return "Email sent successfully! Check your inbox.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @DeleteMapping("/book/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.ok("Deleted Successfully");
    }
}
