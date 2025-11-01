package com.healthcoin.java_eth_demo.service;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// 1. Spring Boot 통합 테스트를 위한 어노테이션
@SpringBootTest
class EthereumServiceTest {

    // 2. 테스트할 서비스 객체를 주입받습니다.
    @Autowired
    private EthereumService ethereumService;

    @Autowired
    private Credentials credentials;

    //ERC-20 contract address
    @Value("${token.contract-address}")
    private String tokenContractAddress;

    // SimpleWallet contract address
    @Value("${simplewallet.contract-address}")
    private String simpleWalletAddress;

    private final String RECIPIENT_ADDRESS = simpleWalletAddress;

    private static final BigDecimal TEST_TOKEN_AMOUNT = new BigDecimal("1.23"); // 테스트에 사용할 토큰 양

    private static String lastTxHash;

    /**
     * (Helper Method)
     * 트랜잭션이 채굴될 때까지 폴링하며 기다리는 헬퍼 메소드 (Step 6 기능 활용)
     */
    private TransactionReceipt waitForTransaction(String txHash) throws Exception {
        System.out.println("Waiting for transaction " + txHash + " to be mined...");
        int attempts = 0;
        int maxAttempts = 10; // 약 50초간 대기
        long pollingInterval = 5000; // 5초

        while (attempts < maxAttempts) {
            TransactionReceipt receipt = ethereumService.getTransactionReceipt(txHash);
            if (receipt != null && receipt.getStatus().equals("0x1")) {
                System.out.println("...Transaction Mined! (Block: " + receipt.getBlockNumber() + ")");
                return receipt;
            } else if (receipt != null && receipt.getStatus().equals("0x0")) {
                throw new RuntimeException("Transaction " + txHash + " failed (Reverted)");
            }

            System.out.println("...Pending (Attempt " + (attempts + 1) + ")");
            Thread.sleep(pollingInterval);
            attempts++;
        }
        throw new RuntimeException("Transaction " + txHash + " was not mined after " + maxAttempts + " attempts.");
    }

    // 3. JUnit 5 테스트 메소드임을 알립니다.
    @Test
    @Order(1)
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
    @Order(2)
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
    @Order(3)
    @DisplayName("특정 주소의 ERC-20 토큰 잔액을 성공적으로 가져와야 한다")
    void getTokenBalance() throws Exception {
        // given: 테스트를 위한 준비
        // 이 주소는 당신이 토큰을 발행했을 때 토큰을 가지고 있는 주소여야 합니다.
        // 보통 컨트랙트를 배포한 당신의 지갑 주소가 됩니다.
        String ownerAddress = "0x0e3293250183089Ec77582310061833Cb1113961";

        // when: 토큰 잔액 조회 실행
        BigDecimal tokenBalance = ethereumService.getTokenBalance(ownerAddress);
        String symbol = ethereumService.getTokenSymbol();

        // then: 결과를 검증
        System.out.println("✅ Token Balance of " + ownerAddress + ": " + tokenBalance + " " + symbol);
        assertThat(tokenBalance).isNotNull(); // 결과가 null이 아니어야 함
        assertThat(tokenBalance).isGreaterThan(BigDecimal.ZERO); // 토큰이 있으므로 0보다 커야 함
    }

//    @Test
//    @DisplayName("다른 주소로 토큰을 성공적으로 전송하고 트랜잭션 해시를 반환해야 한다")
//    void sendToken_shouldReturnTransactionHash() throws Exception {
//        // given: 테스트를 위한 준비
//        // 중요: 이 주소는 실제로 존재하는 당신의 다른 테스트 지갑 주소여야 합니다.
//        String recipientAddress = "0x65ddD397b00548570F2BAea2F0c3Fc47BA23C86F";
//        BigDecimal amountToSend = new BigDecimal("1.5"); // 1.5개의 토큰을 보낸다고 가정
//
//        System.out.println(
//                "Attempting to send " + amountToSend + " tokens from " +
//                        credentials.getAddress() + " to " + recipientAddress + "..."
//        );
//
//        // when: 토큰 전송 실행
//        String txHash = ethereumService.sendToken(recipientAddress, amountToSend);
//
//        // then: 결과를 검증 (트랜잭션 해시가 유효한 형식인지 확인)
//        System.out.println("✅ Transaction submitted! Hash: " + txHash);
//
//        assertThat(txHash).isNotNull();
//        assertThat(txHash).startsWith("0x");
//        assertThat(txHash).hasSize(66); // "0x" 포함 66자
//    }

    @Test
    @Order(4)
    @DisplayName("approve 실행 시 txHash, from, to, spender, amount가 정확히 반환되어야 한다")
    void approveToken_shouldReturnValidReceiptDetails() throws Exception {
        // given: 테스트 준비
        System.out.println("My Server Wallet Address: " + credentials.getAddress());

        // when
        Map<String, String> result = ethereumService.approveToken(RECIPIENT_ADDRESS, TEST_TOKEN_AMOUNT);
        lastTxHash = result.get("transactionHash"); // Step 6에서 사용할 txHash 저장

        // then
        assertThat(lastTxHash).isNotNull().startsWith("0x");
        assertThat(result.get("fromAddress")).isEqualTo(credentials.getAddress());
        assertThat(result.get("toAddress")).isEqualTo(tokenContractAddress);
        assertThat(result.get("spenderAddress")).isEqualTo(RECIPIENT_ADDRESS);

        System.out.println("✅ Approve Tx Submitted: " + lastTxHash);
    }

    @Test
    @Order(3)
    @DisplayName("Step 6: isTransactionSuccessful (Approve 트랜잭션 성공 확인)")
    void testTransactionVerification() throws Exception {
        System.out.println("--- Test: Step 6 (Transaction Verification) ---");
        assertThat(lastTxHash).as("Approve test must run first").isNotNull();

        // when: 트랜잭션이 성공할 때까지 대기
        TransactionReceipt receipt = waitForTransaction(lastTxHash);

        // then: 영수증 상태가 성공(0x1)인지 확인
        assertThat(receipt.isStatusOK()).isTrue();
        System.out.println("✅ Approve Tx Confirmed: " + lastTxHash);
    }

    @Test
    @Order(4)
    @DisplayName("Step 3: Allowance (승인 한도 조회)")
    void testAllowance() throws Exception {
        System.out.println("--- Test: Step 3 (Allowance) ---");
        // given: Order(2)에서 RECIPIENT_ADDRESS에게 승인한 상태
        String ownerAddress = credentials.getAddress();

        // when
        BigDecimal allowance = ethereumService.getAllowance(ownerAddress, RECIPIENT_ADDRESS);
        System.out.println("✅ Allowance for " + RECIPIENT_ADDRESS + ": " + allowance);

        // then
        assertThat(allowance).isGreaterThanOrEqualTo(TEST_TOKEN_AMOUNT);
    }

    @Test
    @Order(5)
    @DisplayName("Step 4: TransferFrom (승인 한도 내에서 대리 전송)")
    void testTransferFrom() throws Exception {
        System.out.println("--- Test: Step 4 (TransferFrom) ---");
        // given: 이 테스트는 서버 지갑이 자신에게 'approve'하고, 자신(spender)이
        // 'recipient'에게 전송하는 시나리오로 구성합니다.

        // 1. Prerequisite: Approve (Spender = Server Wallet, Owner = Server Wallet)
        BigDecimal amount = new BigDecimal("0.1"); // transferFrom 테스트용 소량
        Map<String, String> approveResult = ethereumService.approveToken(credentials.getAddress(), amount);
        waitForTransaction(approveResult.get("transactionHash"));

        BigDecimal initialBalance = ethereumService.getTokenBalance(RECIPIENT_ADDRESS);

        // when: transferFrom 실행
        String txHash = ethereumService.transferTokenFrom(credentials.getAddress(), RECIPIENT_ADDRESS, amount).get("transactionHash");
        waitForTransaction(txHash); // Step 6 (검증) 포함

        // then: RECIPIENT_ADDRESS의 잔액이 증가했는지 확인
        BigDecimal finalBalance = ethereumService.getTokenBalance(RECIPIENT_ADDRESS);
        System.out.println("✅ Recipient Token Balance: " + finalBalance);
        assertThat(finalBalance).isEqualTo(initialBalance.add(amount));
    }

    @Test
    @Order(6)
    @DisplayName("Step 5 & 6: SimpleWallet (ETH 입금 및 출금, 트랜잭션 검증)")
    void testSimpleWalletDepositAndWithdraw() throws Exception {
        System.out.println("--- Test: Step 5 & 6 (SimpleWallet Flow) ---");

        // --- 1. Deposit ---
        System.out.println("Depositing " + TEST_TOKEN_AMOUNT + " ETH to SimpleWallet...");
        String depositTxHash = ethereumService.depositToSimpleWallet(TEST_TOKEN_AMOUNT);

        // --- 2. Verify Deposit (Step 6) ---
        TransactionReceipt depositReceipt = waitForTransaction(depositTxHash);
        assertThat(depositReceipt.isStatusOK()).isTrue();
        System.out.println("✅ Deposit Confirmed: " + depositTxHash);

        // --- 3. Withdraw ---
        System.out.println("Withdrawing " + TEST_TOKEN_AMOUNT + " ETH from SimpleWallet...");
        String withdrawTxHash = ethereumService.withdrawFromSimpleWallet(tokenContractAddress, TEST_TOKEN_AMOUNT);

        // --- 4. Verify Withdraw (Step 6) ---
        TransactionReceipt withdrawReceipt = waitForTransaction(withdrawTxHash);
        assertThat(withdrawReceipt.isStatusOK()).isTrue();
        System.out.println("✅ Withdraw Confirmed: " + withdrawTxHash);
    }
}