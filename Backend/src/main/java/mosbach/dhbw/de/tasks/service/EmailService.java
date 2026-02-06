package mosbach.dhbw.de.tasks.service;

import mosbach.dhbw.de.tasks.model.MealplanConv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optional best-effort admin notifications via SMTP.
 * Disabled by default (mealy.mail.enabled=false).
 */
@Service
public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class.getName());

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String senderEmail;
    private final String adminTo;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${mealy.mail.enabled:false}") boolean enabled,
            @Value("${spring.mail.username:}") String senderEmail,
            @Value("${mealy.mail.adminTo:}") String adminTo
    ) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.senderEmail = senderEmail;
        this.adminTo = adminTo;
    }

    /**
     * Best-effort notification. Never throws to controllers.
     */
    public void sendAdminMealplanSaved(String userEmail, MealplanConv meal, String recipeName) {
        if (!enabled) return;
        if (adminTo == null || adminTo.isBlank()) return;

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            if (senderEmail != null && !senderEmail.isBlank()) {
                msg.setFrom(senderEmail);
            }
            msg.setTo(adminTo);
            msg.setSubject("Mealy: Neue Mealplan-Eintragung");
            msg.setText(buildBody(userEmail, meal, recipeName));
            mailSender.send(msg);
        } catch (MailException e) {
            LOG.log(Level.WARNING, "Mailversand fehlgeschlagen (adminTo=" + adminTo + ")", e);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unerwarteter Fehler beim Mailversand (adminTo=" + adminTo + ")", e);
        }
    }

    private String buildBody(String userEmail, MealplanConv meal, String recipeName) {
        String day = meal != null && meal.getDay() != null ? meal.getDay() : "";
        String time = meal != null && meal.getTime() != null ? meal.getTime() : "";
        String recipeId = meal != null && meal.getId() != null ? meal.getId() : "";

        return "Admin-Notification: Mealplan wurde aktualisiert.\n\n"
                + "User: " + (userEmail != null ? userEmail : "") + "\n"
                + "Datum: " + day + "\n"
                + "Uhrzeit: " + time + "\n"
                + "Rezept-ID: " + recipeId + "\n"
                + "Rezept-Name: " + (recipeName != null ? recipeName : "") + "\n\n"
                + "Zeitstempel: " + Instant.now() + "\n";
    }
}
