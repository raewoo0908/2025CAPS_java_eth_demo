package com.healthcoin.java_eth_demo.DTO.Request;

import lombok.Data;

import java.math.BigInteger;

@Data
public class SubmitRequest {
    private String privateKey;
    private BigInteger round;
    private BigInteger number;
    private BigInteger amountInWei;
}
