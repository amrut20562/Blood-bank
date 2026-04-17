package com.Ranti_Manus.myapplication;

public final class ValidationUtils {
    private ValidationUtils() {
    }

    public static boolean isValidMobile(String mobile) {
        return mobile != null && mobile.trim().matches("[6-9][0-9]{9}");
    }

    public static int parsePositiveQuantity(String quantity) {
        try {
            int parsedQuantity = Integer.parseInt(quantity.trim());
            return parsedQuantity > 0 ? parsedQuantity : -1;
        } catch (Exception e) {
            return -1;
        }
    }
}
