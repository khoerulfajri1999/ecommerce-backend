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
public class PaymentDetailRequest {
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("gross_amount")
    private Long grossAmount;
}
