package com.studyplanner.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Secure password hashing utility using SHA-256 with salt and key stretching.
 * Zero external dependencies.
 */
public class PasswordUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONS = 10000;

    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return bytesToHex(salt);
    }

    public static String hashPassword(String password, String saltHex) {
        if (password == null || saltHex == null) {
            throw new IllegalArgumentException("Password and salt must not be null");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] saltBytes = hexToBytes(saltHex);

            digest.reset();
            digest.update(saltBytes);
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Key stretching iterations
            for (int i = 0; i < ITERATIONS; i++) {
                digest.reset();
                hashed = digest.digest(hashed);
            }

            return bytesToHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }

    public static boolean verifyPassword(String password, String saltHex, String expectedHash) {
        if (password == null || saltHex == null || expectedHash == null) {
            return false;
        }
        String calculated = hashPassword(password, saltHex);
        return MessageDigest.isEqual(calculated.getBytes(StandardCharsets.UTF_8),
                                     expectedHash.getBytes(StandardCharsets.UTF_8));
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
