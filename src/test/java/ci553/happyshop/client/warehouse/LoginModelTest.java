package ci553.happyshop.client.warehouse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for issue #5: Warehouse login, password hashing and the 3-minute lock.
 * Uses a test password, never the real one.
 */
class LoginModelTest {

    /** A clock the tests can move forward, so they don't really wait 3 minutes. */
    static class TestClock extends Clock {
        private Instant now = Instant.parse("2026-11-17T12:00:00Z");
        void advance(Duration d) { now = now.plus(d); }
        @Override public Instant instant() { return now; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
    }

    private static final String USER = "staff";
    private static final String PASSWORD = "Test-Pass#123";

    private TestClock clock;
    private LoginModel model;

    @BeforeEach
    void setUp() {
        String salt = PasswordHasher.newSalt();
        String hash = PasswordHasher.hash(PASSWORD, salt);
        clock = new TestClock();
        model = new LoginModel(USER, salt, hash, clock);
    }

    @Test
    void correctDetailsLogIn() {
        assertTrue(model.login(USER, PASSWORD));
    }

    @Test
    void wrongPasswordFailsAndCountsDown() {
        assertFalse(model.login(USER, "wrong"));
        assertTrue(model.getMessage().contains("2 attempt(s) left"));
    }

    @Test
    void wrongUsernameGivesSameMessageAsWrongPassword() {
        model.login("nobody", PASSWORD);
        String wrongUserMessage = model.getMessage();

        setUp(); // fresh model, so the attempt counts match
        model.login(USER, "wrong");

        assertEquals(model.getMessage(), wrongUserMessage);
    }

    @Test
    void emptyFieldsAreRejectedWithoutCountingAnAttempt() {
        model.login("", "");
        model.login("", "");
        model.login("", "");

        assertTrue(model.login(USER, PASSWORD)); // not locked
    }

    @Test
    void threeFailuresLockEvenTheCorrectPassword() {
        model.login(USER, "wrong1");
        model.login(USER, "wrong2");
        model.login(USER, "wrong3");

        assertFalse(model.login(USER, PASSWORD));
        assertTrue(model.getMessage().contains("Try again in"));
    }

    @Test
    void stillLockedJustBeforeThreeMinutes() {
        model.login(USER, "wrong1");
        model.login(USER, "wrong2");
        model.login(USER, "wrong3");

        clock.advance(Duration.ofMinutes(2).plusSeconds(59));
        assertFalse(model.login(USER, PASSWORD));
    }

    @Test
    void unlocksAfterThreeMinutes() {
        model.login(USER, "wrong1");
        model.login(USER, "wrong2");
        model.login(USER, "wrong3");

        clock.advance(Duration.ofMinutes(3));
        assertTrue(model.login(USER, PASSWORD));
    }

    @Test
    void sameePasswordWithDifferentSaltsGivesDifferentHashes() {
        String hash1 = PasswordHasher.hash(PASSWORD, PasswordHasher.newSalt());
        String hash2 = PasswordHasher.hash(PASSWORD, PasswordHasher.newSalt());

        assertNotEquals(hash1, hash2);
        assertNotEquals(PASSWORD, hash1); // the hash never reveals the password
    }
}