package mosbach.dhbw.de.tasks.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optional best-effort SMTP notifications.
 * Disabled by default (mealy.mail.enabled=false).
 */
@Service
public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class.getName());

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String senderEmail;
    private final String overrideTo;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${mealy.mail.enabled:false}") boolean enabled,
            @Value("${spring.mail.username:}") String senderEmail,
            @Value("${mealy.mail.overrideTo:}") String overrideTo
    ) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.senderEmail = senderEmail;
        this.overrideTo = overrideTo;
    }

    /**
     * Sends a confirmation to the user after creating a recipe.
     * Best-effort: never throws to controllers.
     */
    public void sendRecipeCreated(String userEmail, String recipeName) {
        if (!enabled) return;

        String to = (overrideTo != null && !overrideTo.isBlank()) ? overrideTo : userEmail;
        if (to == null || to.isBlank()) return;

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            if (senderEmail != null && !senderEmail.isBlank()) {
                msg.setFrom(senderEmail);
            }
            msg.setTo(to);
            msg.setSubject("Mealy: Rezept erstellt");
            msg.setText(buildRecipeCreatedBody(userEmail, recipeName));
            mailSender.send(msg);
        } catch (MailException e) {
            LOG.log(Level.WARNING, "Mailversand fehlgeschlagen (to=" + to + ")", e);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unerwarteter Fehler beim Mailversand (to=" + to + ")", e);
        }
    }

    private String buildRecipeCreatedBody(String userEmail, String recipeName) {
        return "Dein Rezept wurde erfolgreich erstellt.\n\n"
                + "User: " + (userEmail != null ? userEmail : "") + "\n"
                + "Rezept: " + (recipeName != null ? recipeName : "") + "\n\n"
                + "Zeitstempel: " + Instant.now() + "\n";
    }
}
