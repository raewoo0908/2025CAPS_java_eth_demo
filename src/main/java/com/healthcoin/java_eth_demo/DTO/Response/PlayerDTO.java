package com.healthcoin.java_eth_demo.DTO.Response;

import com.healthcoin.java_eth_demo.contracts.TOGame;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

@Data
@AllArgsConstructor
public class PlayerDTO {

    private String playerAddress;
    private BigInteger amount; // (TOGame.Player 구조체 필드명에 맞게)

    // TOGame.Player에서 DTO를 생성하는 정적 팩토리 메소드
    public static PlayerDTO fromPlayer(TOGame.Player player) {
        // 래퍼의 Player 클래스 필드명에 따라 player.playerAddress, player.amount로 가정
        // 만약 component1(), component2() 라면
        // return new PlayerDto(player.component1(), player.component2());
        return new PlayerDTO(player.playerAddress, player.amount);
    }
}
