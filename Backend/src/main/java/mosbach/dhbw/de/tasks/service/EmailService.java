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
     * Sends a confirmation after creating a recipe.
     *
     * IMPORTANT: This method intentionally NEVER sends to the user address.
     * It always sends to mealy.mail.overrideTo (best-effort).
     */
    public void sendRecipeCreated(String userEmail, String recipeName) {
        if (!enabled) return;

        String to = (overrideTo != null) ? overrideTo.trim() : "";
        if (to.isBlank()) {
            LOG.log(Level.WARNING, "Mail skipped: mealy.mail.overrideTo is empty (sendRecipeCreated)");
            return;
        }

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

    /**
     * Sends a notification to the recipe owner when someone rated the recipe.
     * If mealy.mail.overrideTo is set, the email will be sent there instead (useful for testing).
     */
    public void sendRecipeRated(
            String ownerEmail,
            String ownerUserName,
            String recipeName,
            String raterEmail,
            String raterUserName,
            int stars,
            String comment
    ) {
        if (!enabled) return;

        String to = (overrideTo != null && !overrideTo.isBlank())
                ? overrideTo.trim()
                : (ownerEmail != null ? ownerEmail.trim() : "");
        if (to.isBlank()) return;

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            if (senderEmail != null && !senderEmail.isBlank()) {
                msg.setFrom(senderEmail);
            }
            msg.setTo(to);
            msg.setSubject("Mealy: Dein Rezept wurde bewertet (" + stars + "★)");
            msg.setText(buildRecipeRatedBody(ownerEmail, ownerUserName, recipeName, raterEmail, raterUserName, stars, comment));
            mailSender.send(msg);
        } catch (MailException e) {
            LOG.log(Level.WARNING, "Mailversand fehlgeschlagen (to=" + to + ")", e);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unerwarteter Fehler beim Mailversand (to=" + to + ")", e);
        }
    }

    private String buildRecipeCreatedBody(String userEmail, String recipeName) {
        return "Dein Rezept wurde erfolgreich erstellt.\n\n"
                + "User: " + safe(userEmail) + "\n"
                + "Rezept: " + safe(recipeName) + "\n\n"
                + "Zeitstempel: " + Instant.now() + "\n";
    }

    private String buildRecipeRatedBody(
            String ownerEmail,
            String ownerUserName,
            String recipeName,
            String raterEmail,
            String raterUserName,
            int stars,
            String comment
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dein Rezept wurde bewertet.\n\n");
        sb.append("Owner: ").append(safe(ownerUserName)).append(" (").append(safe(ownerEmail)).append(")\n");
        sb.append("Rezept: ").append(safe(recipeName)).append("\n");
        sb.append("Bewertung: ").append(stars).append(" von 5\n");
        sb.append("Von: ").append(safe(raterUserName)).append(" (").append(safe(raterEmail)).append(")\n");
        if (comment != null && !comment.isBlank()) {
            sb.append("\nKommentar:\n").append(comment.trim()).append("\n");
        }
        sb.append("\nZeitstempel: ").append(Instant.now()).append("\n");
        return sb.toString();
    }

    private String safe(String s) {
        return s != null ? s : "";
    }
}
