package com.khoerulfajri.model;

import lombok.Getter;
import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public enum StatusTransaksi {
    ORDERED("ordered", "Ordered"),
    SETTLEMENT("settlement", "Settlement"),
    PENDING("pending", "Pending"),
    CANCEL("cancel", "Cancel"),
    FAILURE("failure", "Failure"),
    EXPIRED("expired", "Expired"),
    DENY("deny", "Deny");

    private final String name;
    private final String description;

    StatusTransaksi(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Method untuk mendapatkan daftar status dalam format SQL CHECK
    public static String getSqlCheckConstraint() {
        return Arrays.stream(StatusTransaksi.values())
                .map(status -> "'" + status.name + "'") // Formatkan jadi 'ordered', 'settlement', dll
                .collect(Collectors.joining(", "));
    }
}
