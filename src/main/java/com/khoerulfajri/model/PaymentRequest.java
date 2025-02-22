package com.khoerulfajri.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRequest {
    @JsonProperty("transaction_details")
    private PaymentDetailRequest paymentDetail;

    @JsonProperty("customer_details")
    private PenggunaRequest penggunaRequest;
}

