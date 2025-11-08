package com.healthcoin.java_eth_demo.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // 값을 받는 생성자
public class ValueDTO<T> {

    private T value;

    // 사용 예시 (Controller에서)
    // ValueDto<String> dto = new ValueDto<>("0x123...");
    // ValueDto<BigInteger> dto = new ValueDto<>(BigInteger.ONE);
}
