package com.khoerulfajri.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class KeranjangRequest implements Serializable {

    private String produkId;
    private Integer kuantitas;

}
