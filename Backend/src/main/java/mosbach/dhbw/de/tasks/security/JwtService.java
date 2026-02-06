package mosbach.dhbw.de.tasks.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final Duration expiration;

    public JwtService(
            @Value("${mealy.jwt.secret:}") String secret,
            @Value("${mealy.jwt.expirationMinutes:10080}") long expirationMinutes
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("mealy.jwt.secret must not be blank");
        }
        if (secret.length() < 32) {
            throw new IllegalArgumentException("mealy.jwt.secret must be at least 32 characters long (HS256) ");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    public String generateToken(String subjectEmail, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration.toMillis());

        return Jwts.builder()
                .subject(subjectEmail)
                .issuedAt(now)
                .expiration(exp)
                .claims(extraClaims)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
