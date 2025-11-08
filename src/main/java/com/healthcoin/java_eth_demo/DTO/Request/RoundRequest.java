package com.healthcoin.java_eth_demo.DTO.Request;

import lombok.Data;

import java.math.BigInteger;

@Data
public class RoundRequest {
    private String privateKey;
    private BigInteger roundId;
}
