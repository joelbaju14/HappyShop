package ci553.happyshop.client.warehouse;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;

/**
 * Hashes passwords with PBKDF2 and a random salt, so only the salt and hash
 * are stored, never the password itself.
 */
public final class PasswordHasher {

    private static final int ITERATIONS = 100_000; // slows down each guess
    private static final int KEY_LENGTH = 256;     // bits

    private PasswordHasher() { } // utility class: no objects needed

    /**
     * Creates a new random salt.
     * @return the salt as Base64 text
     */
    public static String newSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password with the given salt using PBKDF2.
     * @param password the password typed by the user
     * @param salt     the stored salt (Base64)
     * @return the hash as Base64 text
     */
    public static String hash(String password, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(),
                    Base64.getDecoder().decode(salt), ITERATIONS, KEY_LENGTH);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 is not available", e);
        }
    }

    /**
     * Checks a typed password against a stored salt and hash.
     * @return true if the password is correct
     */
    public static boolean matches(String password, String salt, String expectedHash) {
        String actual = hash(password, salt);
        // compares in constant time, so timing can't leak how much of the hash matched
        return MessageDigest.isEqual(actual.getBytes(), expectedHash.getBytes());
    }

    /**
     * Run this once to create the salt and hash for a new password.
     * The password is typed in at run time, so it never appears in the code.
     */
    public static void main(String[] args) {
        System.out.print("New password: ");
        String password = new Scanner(System.in).nextLine();
        String salt = newSalt();
        System.out.println("SALT = " + salt);
        System.out.println("HASH = " + hash(password, salt));
    }
}