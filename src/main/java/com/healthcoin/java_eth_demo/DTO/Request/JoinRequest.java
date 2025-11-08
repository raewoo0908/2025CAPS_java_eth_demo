package com.healthcoin.java_eth_demo.DTO.Request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class JoinRequest {
    private String privateKey;
    private String tokenContractAddress;
    private String spenderAddress;
    private BigDecimal amountInETH;
}
