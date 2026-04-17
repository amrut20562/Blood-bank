package com.Ranti_Manus.myapplication;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class BloodCompatibility {
    private BloodCompatibility() {
    }

    public static boolean canDonateTo(String donorGroup, String recipientGroup) {
        donorGroup = normalize(donorGroup);
        recipientGroup = normalize(recipientGroup);

        switch (donorGroup) {
            case "O-":
                return true;
            case "O+":
                return isOneOf(recipientGroup, "O+", "A+", "B+", "AB+");
            case "A-":
                return isOneOf(recipientGroup, "A-", "A+", "AB-", "AB+");
            case "A+":
                return isOneOf(recipientGroup, "A+", "AB+");
            case "B-":
                return isOneOf(recipientGroup, "B-", "B+", "AB-", "AB+");
            case "B+":
                return isOneOf(recipientGroup, "B+", "AB+");
            case "AB-":
                return isOneOf(recipientGroup, "AB-", "AB+");
            case "AB+":
                return "AB+".equals(recipientGroup);
            default:
                return false;
        }
    }

    public static Set<String> compatibleDonorGroupsFor(String recipientGroup) {
        Set<String> groups = new HashSet<>();
        for (String donorGroup : new String[]{"O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"}) {
            if (canDonateTo(donorGroup, recipientGroup)) {
                groups.add(donorGroup);
            }
        }
        return groups;
    }

    private static boolean isOneOf(String value, String... values) {
        return Arrays.asList(values).contains(value);
    }

    private static String normalize(String group) {
        return group == null ? "" : group.trim().toUpperCase();
    }
}
