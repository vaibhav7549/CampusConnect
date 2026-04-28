package com.vaibhav.campusserviceapp.utils;

import java.util.Arrays;
import java.util.List;

public class VerifiedDomains {
    private static final List<String> VERIFIED_DOMAINS = Arrays.asList(
            ".edu",
            ".edu.in",
            ".ac.in",
            ".ac.uk",
            ".edu.au",
            ".edu.pk",
            ".edu.ng",
            ".edu.sg",
            ".edu.my",
            ".edu.ph"
    );

    public static boolean isVerifiedEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        for (String verifiedDomain : VERIFIED_DOMAINS) {
            if (domain.endsWith(verifiedDomain)) {
                return true;
            }
        }
        return false;
    }
}
