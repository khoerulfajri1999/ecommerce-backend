package com.khoerulfajri.model;

public enum StatusPesanan {

    DRAFT, PACKING, PEMBAYARAN, PENGIRIMAN, SELESAI, DIBATALKAN;

    public static boolean isDraftOrSettlement(String status) {
        return status.equalsIgnoreCase("DRAFT") || status.equalsIgnoreCase("SETTLEMENT");
    }

    public static boolean isDibatalkanOrRefund(String status) {
        return status.equalsIgnoreCase("DIBATALKAN") || status.equalsIgnoreCase("REFUND");
    }
}
