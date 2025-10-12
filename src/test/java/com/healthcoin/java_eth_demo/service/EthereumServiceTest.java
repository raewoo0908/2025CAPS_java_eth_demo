package com.healthcoin.java_eth_demo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

// 1. Spring Boot 통합 테스트를 위한 어노테이션
@SpringBootTest
class EthereumServiceTest {

    // 2. 테스트할 서비스 객체를 주입받습니다.
    @Autowired
    private EthereumService ethereumService;

    // 3. JUnit 5 테스트 메소드임을 알립니다.
    @Test
    @DisplayName("최신 블록 넘버를 성공적으로 가져와야 한다") // 4. 테스트에 대한 설명
    void getLatestBlockNumber() throws Exception {
        // when: 테스트하려는 동작을 수행
        BigInteger blockNumber = ethereumService.getLatestBlockNumber();

        // then: 결과를 검증
        System.out.println("✅ Latest Block Number: " + blockNumber);
        assertThat(blockNumber).isNotNull(); // 결과가 null이 아니어야 함
        assertThat(blockNumber).isGreaterThan(BigInteger.ZERO); // 0보다 커야 함
    }

    @Test
    @DisplayName("특정 주소의 ETH 잔액을 성공적으로 가져와야 한다")
    void getEthBalance() throws Exception {
        // given: 테스트를 위한 준비
        String testAddress = "0x0e3293250183089Ec77582310061833Cb1113961"; // My address

        // when: 테스트하려는 동작을 수행
        BigDecimal balance = ethereumService.getEthBalance(testAddress);

        // then: 결과를 검증
        System.out.println("✅ ETH Balance of " + testAddress + ": " + balance + " ETH");
        assertThat(balance).isNotNull(); // 결과가 null이 아니어야 함
        assertThat(balance).isGreaterThanOrEqualTo(BigDecimal.ZERO); // 0 이상이어야 함
    }

    @Test
    @DisplayName("특정 주소의 ERC-20 토큰 잔액을 성공적으로 가져와야 한다")
    void getTokenBalance() throws Exception {
        // given: 테스트를 위한 준비
        // 이 주소는 당신이 토큰을 발행했을 때 토큰을 가지고 있는 주소여야 합니다.
        // 보통 컨트랙트를 배포한 당신의 지갑 주소가 됩니다.
        String ownerAddress = "0x0e3293250183089Ec77582310061833Cb1113961";

        // when: 토큰 잔액 조회 실행
        BigDecimal tokenBalance = ethereumService.getTokenBalance(ownerAddress);

        // then: 결과를 검증
        System.out.println("✅ Token Balance of " + ownerAddress + ": " + tokenBalance + " MFT");
        assertThat(tokenBalance).isNotNull(); // 결과가 null이 아니어야 함
        assertThat(tokenBalance).isGreaterThan(BigDecimal.ZERO); // 토큰이 있으므로 0보다 커야 함
    }
}