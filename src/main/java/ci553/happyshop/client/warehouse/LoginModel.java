package ci553.happyshop.client.warehouse;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Checks staff login details for the Warehouse client and locks login
 * for 3 minutes after 3 failed attempts.
 */
public class LoginModel {

    static final int MAX_ATTEMPTS = 3;
    static final Duration LOCK_TIME = Duration.ofMinutes(3);

    private final String username;
    private final String salt;
    private final String passwordHash;
    private final Clock clock; // injected, so tests can control time

    private int failedAttempts = 0;
    private Instant lockedUntil = null;
    private String message = "Please log in.";

    /**
     * @param username     the staff username
     * @param salt         the stored salt (Base64)
     * @param passwordHash the stored PBKDF2 hash (Base64)
     * @param clock        the clock used for the lock-out timer
     */
    public LoginModel(String username, String salt, String passwordHash, Clock clock) {
        this.username = username;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.clock = clock;
    }

    /**
     * Tries to log in.
     * @return true if the details are correct and login is not locked
     */
    boolean login(String enteredUser, String enteredPassword) {
        Instant now = clock.instant();

        // 1. Locked out?
        if (lockedUntil != null && now.isBefore(lockedUntil)) {
            long seconds = Duration.between(now, lockedUntil).toSeconds() + 1;
            message = "Too many failed attempts. Try again in " + seconds + " seconds.";
            return false;
        }

        // 2. Empty fields
        if (enteredUser == null || enteredUser.isBlank()
                || enteredPassword == null || enteredPassword.isEmpty()) {
            message = "Please enter your username and password.";
            return false;
        }

        // 3. Correct details
        if (enteredUser.trim().equals(username)
                && PasswordHasher.matches(enteredPassword, salt, passwordHash)) {
            failedAttempts = 0;
            lockedUntil = null;
            message = "Login successful.";
            return true;
        }

        // 4. Wrong details: count the attempt, lock after 3
        failedAttempts++;
        if (failedAttempts >= MAX_ATTEMPTS) {
            lockedUntil = now.plus(LOCK_TIME);
            failedAttempts = 0;
            message = "Too many failed attempts. Login is locked for 3 minutes.";
        } else {
            message = "Incorrect username or password. "
                    + (MAX_ATTEMPTS - failedAttempts) + " attempt(s) left.";
        }
        return false;
    }

    /** @return the message to show the user */
    String getMessage() {
        return message;
    }
}