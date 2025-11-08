package com.healthcoin.java_eth_demo.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.web3j.tuples.generated.Tuple4;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;

@Data
@AllArgsConstructor // 모든 필드를 받는 생성자
public class RoundInfoDTO{

    private BigInteger currentIndex;
    private BigInteger prizePool;
    private boolean isGameOver;
    private BigInteger winnerPercentage;

    // Map에서 DTO를 생성하는 정적 팩토리 메소드
    public static RoundInfoDTO fromMap(Map<String, Object> map) {
        return new RoundInfoDTO(
                (BigInteger) map.get("currentIndex"),
                (BigInteger) map.get("prizePool"),
                (Boolean) map.get("isGameOver"),
                (BigInteger) map.get("winnerPercentage")
        );
    }

    // Tuple4에서 DTO를 생성하는 정적 팩토리 메소드
    public static RoundInfoDTO fromTuple(Tuple4<BigInteger, BigInteger, Boolean, BigInteger> tuple) {
        return new RoundInfoDTO(
                tuple.component1(),
                tuple.component2(),
                tuple.component3(),
                tuple.component4()
        );
    }
}