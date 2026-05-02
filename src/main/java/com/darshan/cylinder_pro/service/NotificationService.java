package com.darshan.cylinder_pro.service;

import com.darshan.cylinder_pro.model.Booking;
import com.darshan.cylinder_pro.repo.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private BookingRepository repo;

    @Autowired
    private CylinderService cylinderService;
    
    // Cron expression for 9:00 AM every day
//    @Scheduled(cron = "0 0 9 * * *")
//    @Scheduled(cron = "0 0 8 * * *")
   @Scheduled(cron = "0 36 22 * * *", zone = "Asia/Kolkata")
    public void runDailyCheck() {
        List<String> allUsers = repo.findAllUniqueDeviceTokens();

        for (String token : allUsers) {
            List<Booking> history = repo.findByDeviceTokenOrderByBookingDateAsc(token);
            if (history.isEmpty()) continue;


            Booking latestBooking = history.get(history.size() - 1);
            String userEmail = latestBooking.getEmail();
            String providerName = latestBooking.getProviderName(); // ADD THIS LINE

            if (userEmail == null || userEmail.isEmpty()) continue;

            LocalDate nextDate = cylinderService.predictNextDate(token);
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), nextDate);


            if (daysLeft <= 3) {
                String subject;
                String body;

                // Check the language stored for this user/booking
                String language = latestBooking.getLanguage();
                if ("kn".equals(language)) {
                    // --- KANNADA VERSION ---
                    subject = "🔥 ಸಿಲಿಂಡರ್ ಪ್ರೊ: ರೀಫಿಲ್ ರಿಮೈಂಡರ್";
                    body = "ನಮಸ್ಕಾರ,\n\n" +
                            "ಇದು ನಿಮ್ಮ ಸಿಲಿಂಡರ್ ಪ್ರೊ ಅಸಿಸ್ಟೆಂಟ್‌ನಿಂದ ಬಂದ ಜ್ಞಾಪನೆ.\n\n" +
                            "📅 ಸದ್ಯದ ಸ್ಥಿತಿ:\n" +
                            "ನಿಮ್ಮ " + providerName + " ಸಿಲಿಂಡರ್ ಖಾಲಿಯಾಗಲು ಅಂದಾಜು " + daysLeft + " ದಿನಗಳು ಮಾತ್ರ ಬಾಕಿ ಇವೆ.\n\n" +
                            "💡 ಸಲಹೆ:\n" +
                            "⚠️ ನಿಮ್ಮ ಗ್ಯಾಸ್ ತುಂಬಾ ಕಡಿಮೆ ಇದೆ! ಇಂದೇ ಬುಕ್ ಮಾಡುವುದು ಉತ್ತಮ.\n\n" +
                            "ಧನ್ಯವಾದಗಳು,\nಸಿಲಿಂಡರ್ ಪ್ರೊ ತಂಡ";
                } else {
                    // --- ENGLISH VERSION ---
                    subject = "🔥 Cylinder Pro: Refill Reminder";
                    body = "Hello,\n\n" +
                            "This is a reminder from your Cylinder Pro Assistant.\n\n" +
                            "📅 Status Update:\n" +
                            "You have approximately " + daysLeft + " days remaining for your " + providerName + " cylinder.\n\n" +
                            "💡 Suggestion:\n" +
                            "⚠️ Your gas is very low! Better book today.\n\n" +
                            "Stay prepared,\nCylinder Pro Team";
                }

                sendEmail(userEmail, subject, body);
            }
        }
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("darshancmp356@gmail.com"); // Match your application.properties
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
