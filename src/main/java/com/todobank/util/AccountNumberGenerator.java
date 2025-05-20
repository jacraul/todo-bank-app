package com.todobank.util;

import java.security.SecureRandom;

public class AccountNumberGenerator {
    public static String generateIBAN() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder("ROXXTODO");
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}