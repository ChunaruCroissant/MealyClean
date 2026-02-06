package mosbach.dhbw.de.tasks.data.impl;

import mosbach.dhbw.de.tasks.data.api.UserIF;
import mosbach.dhbw.de.tasks.model.TokenConv;
import mosbach.dhbw.de.tasks.model.UserConv;
import mosbach.dhbw.de.tasks.security.JwtService;
import mosbach.dhbw.de.tasks.persistence.entity.UserEntity;
import mosbach.dhbw.de.tasks.persistence.repo.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UserManager {

    private final UserRepository userRepo;

    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserManager(UserRepository userRepo, JwtService jwtService) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserEntity addUser(UserIF user) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        if (userRepo.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already used");
        }

        UserEntity e = new UserEntity();
        e.setUserName(user.getUserName());
        e.setEmail(user.getEmail());

        // Store BCrypt hash (not plain)
        e.setPasswordHash(passwordEncoder.encode(user.getPassword()));

        return userRepo.save(e);
    }

    /**
     * Authenticate by username or email + password.
     * Also upgrades legacy plain-text passwordHash values to BCrypt.
     */
    @Transactional
    public UserEntity authenticate(UserConv data) {
        if (data == null) return null;
        String identifier = null;
        if (data.getEmail() != null && !data.getEmail().isBlank()) identifier = data.getEmail().trim();
        else if (data.getUserName() != null && !data.getUserName().isBlank()) identifier = data.getUserName().trim();
        if (identifier == null || data.getPassword() == null) return null;

        UserEntity u = identifier.contains("@")
                ? userRepo.findByEmail(identifier).orElse(null)
                : userRepo.findFirstByUserName(identifier).orElse(null);
        if (u == null) return null;
        return passwordsMatchAndUpgradeIfNeeded(data.getPassword(), u) ? u : null;
    }

    @Transactional(readOnly = true)
    public UserConv searchUserByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return userRepo.findByEmail(email)
                .map(u -> new UserConv(u.getUserName(), u.getEmail(), u.getPasswordHash()))
                .orElse(null);
    }

    // ==== Token-Teil (Phase 3: JWT) ====

    public String issueToken(UserEntity user) {
        if (user == null) return null;
        return jwtService.generateToken(
                user.getEmail(),
                Map.of(
                        "uid", user.getId(),
                        "userName", user.getUserName() != null ? user.getUserName() : ""
                )
        );
    }

    public boolean checkToken(TokenConv token) {
        if (token == null || token.getToken() == null || token.getToken().isBlank()) return false;
        String jwt = token.getToken();
        if (!jwtService.isTokenValid(jwt)) return false;
        String email = jwtService.extractSubject(jwt);
        return email != null && userRepo.existsByEmail(email);
    }

    public UserConv TokenToUser(String token) {
        if (token == null || token.isBlank()) return null;
        if (!jwtService.isTokenValid(token)) return null;
        String email = jwtService.extractSubject(token);
        if (email == null) return null;
        return searchUserByEmail(email);
    }

    @Transactional
    public UserEntity updateUserForTokenOwner(UserConv tokenOwner, UserConv update) {
        if (tokenOwner == null || tokenOwner.getEmail() == null) return null;
        UserEntity e = userRepo.findByEmail(tokenOwner.getEmail()).orElse(null);
        if (e == null) return null;

        if (update != null) {
            if (update.getUserName() != null && !update.getUserName().isBlank()) {
                e.setUserName(update.getUserName());
            }

            // Email change is allowed in Phase 3 (JWT). Old token will become invalid.
            if (update.getEmail() != null && !update.getEmail().isBlank() && !update.getEmail().equals(e.getEmail())) {
                if (userRepo.existsByEmail(update.getEmail())) {
                    throw new IllegalArgumentException("Email already used");
                }
                e.setEmail(update.getEmail());
            }

            if (update.getPassword() != null && !update.getPassword().isBlank()) {
                e.setPasswordHash(passwordEncoder.encode(update.getPassword()));
            }
        }

        return userRepo.save(e);
    }

    @Transactional
    public boolean deleteUserByEmail(String email) {
        if (email == null || email.isBlank()) return false;
        UserEntity e = userRepo.findByEmail(email).orElse(null);
        if (e == null) return false;
        userRepo.delete(e);
        return true;
    }

    private boolean passwordsMatchAndUpgradeIfNeeded(String rawPassword, UserEntity userEntity) {
        if (rawPassword == null || userEntity == null) return false;
        String stored = userEntity.getPasswordHash();
        if (stored == null || stored.isBlank()) return false;

        // BCrypt hashes start with $2a$ / $2b$ / $2y$
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, stored);
        }

        // Legacy plain-text fallback: compare and upgrade
        if (rawPassword.equals(stored)) {
            userEntity.setPasswordHash(passwordEncoder.encode(rawPassword));
            userRepo.save(userEntity);
            return true;
        }
        return false;
    }
}
