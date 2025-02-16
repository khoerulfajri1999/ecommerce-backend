package com.khoerulfajri.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PesananRequestSingle implements Serializable {

    private BigDecimal ongkir;
    private String alamatPengiriman;
    private String produkId;
    private Integer kuantitas = 1;

}
