package com.khoerulfajri.util.validation;

public class PagingUtil {
    public static Integer validatePage(String page) {
        try {
            int parsedPage = Integer.parseInt(page);
            return Math.max(parsedPage, 0); // Indeks berbasis 0
        } catch (NumberFormatException e) {
            return 0; // Default ke halaman pertama
        }
    }

    public static Integer validateSize(String size) {
        try {
            int parsedSize = Integer.parseInt(size);
            return parsedSize <= 0 ? 10 : parsedSize; // Default ke 10 jika invalid
        } catch (NumberFormatException e) {
            return 10; // Default ke 10 jika format salah
        }
    }

    public static String validateDirection(String direction) {
        return direction.equalsIgnoreCase("ASC") || direction.equalsIgnoreCase("DESC") ? direction : "ASC";
    }
}
