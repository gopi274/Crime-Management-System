package com.crimemanagement.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for generating and verifying password hashes using BCrypt.
 * 
 * This class provides methods for password hashing and verification.
 * Used for generating password hashes for user accounts.
 */
public class HashGenerator {
    /**
     * Main method for generating password hash and verifying passwords.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        String password = "admin123";

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);

        String inputPassword = "admin123";
        if (BCrypt.checkpw(inputPassword, hash)) {
            System.out.println("Password matches");
        } else {
            System.out.println("Password does NOT match");
        }
    }
}

