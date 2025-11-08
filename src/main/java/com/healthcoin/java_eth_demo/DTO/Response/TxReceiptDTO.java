package com.healthcoin.java_eth_demo.DTO.Response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;

@Data
@NoArgsConstructor // 기본 생성자
public class TxReceiptDTO {

    private String transactionHash;
    private boolean success;
    private BigInteger blockNumber;
    private String from;
    private String to;
    private BigInteger gasUsed;

    // TransactionReceipt 객체에서 DTO를 생성하는 정적 팩토리 메소드
    public static TxReceiptDTO fromReceipt(TransactionReceipt receipt) {
        TxReceiptDTO dto = new TxReceiptDTO();
        dto.setTransactionHash(receipt.getTransactionHash());
        dto.setSuccess(receipt.isStatusOK()); // 0x1 (true) or 0x0 (false)
        dto.setBlockNumber(receipt.getBlockNumber());
        dto.setFrom(receipt.getFrom());
        dto.setTo(receipt.getTo());
        dto.setGasUsed(receipt.getGasUsed());
        return dto;
    }
}
