package com.healthcoin.java_eth_demo.service;

import com.healthcoin.java_eth_demo.config.Web3jConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ContextConfiguration(classes = {EthereumService.class, Web3jConfig.class}) // Service와 Config를 로드
class EthereumServiceIntegrationTest {

    // --- Injected Beans & Properties ---
    @Autowired
    private EthereumService ethereumService;

    @Autowired
    private Web3j web3j; // web3j 연결 확인용

    @Autowired
    private Credentials myWalletCredentials; // Owner Wallet (Config에서 주입)

    @Autowired
    private Credentials test2Credentials;

    @Value("${token.mztt-contract-address}")
    private String tokenContractAddress;

    @Value("${simplewallet.contract-address}")
    private String simpleWalletAddress;

    // --- Test Actors ---
    private Credentials recipientCredentials;
    private Credentials spenderCredentials;
    private String ownerAddress;
    private String recipientAddress;
    private String spenderAddress;

    // --- Test Constants ---
    private static final BigDecimal AMOUNT_TO_SEND = new BigDecimal("0.1");
    private static final BigDecimal AMOUNT_TO_APPROVE = new BigDecimal("100");
    private static final BigDecimal AMOUNT_FOR_DEPOSIT = new BigDecimal("10.5");
    private static final BigDecimal AMOUNT_FOR_WITHDRAW = new BigDecimal("5.5");

    // --- Shared State (for ordered tests) ---
    private static BigDecimal recipientBalanceBeforeSend;
    private static BigDecimal walletBalanceBeforeWithdraw;

    @BeforeEach
    void setUp() {
        // 매 테스트 전에 Actor 정보 초기화
        this.recipientCredentials = test2Credentials;
        this.spenderCredentials = test2Credentials;
        this.recipientAddress = recipientCredentials.getAddress();
        this.ownerAddress = myWalletCredentials.getAddress();
        this.spenderAddress = spenderCredentials.getAddress();

        System.out.println("--- Test Setup ---");
        System.out.println("Owner Wallet: " + ownerAddress);
        System.out.println("Recipient/Spender Wallet: " + recipientAddress);
        System.out.println("Token CA: " + tokenContractAddress);
        System.out.println("SimpleWallet CA: " + simpleWalletAddress);
        System.out.println("--------------------");
    }

    // 1. READ-ONLY 작업 테스트 (연결 및 기본 정보)
    @Test
    @Order(1)
    @DisplayName("Step 1: Get Connection & Read-Only Data")
    void testConnectionAndBasicInfo() throws Exception {
        System.out.println("--- Test: Step 1 (Read-Only) ---");

        // 1.1. Get Latest Block Number
        BigInteger blockNumber = ethereumService.getLatestBlockNumber();
        System.out.println("Latest Block: " + blockNumber);
        assertThat(blockNumber).isGreaterThan(BigInteger.ZERO);

        // 1.2. Get ETH Balance
        BigDecimal ethBalance = ethereumService.getEthBalance(ownerAddress);
        System.out.println("Owner ETH Balance: " + ethBalance);
        assertThat(ethBalance).isNotNull(); // 0일 수는 있지만 null이면 안 됨

        // 1.3. Get Token Name
        String name = ethereumService.getTokenName(tokenContractAddress, myWalletCredentials);
        System.out.println("Token Name: " + name);
        assertThat(name).isNotNull().isNotEmpty(); // "RaewooCoin" 등

        // 1.4. Get Token Symbol
        String symbol = ethereumService.getTokenSymbol(tokenContractAddress, myWalletCredentials);
        System.out.println("Token Symbol: " + symbol);
        assertThat(symbol).isNotNull().isNotEmpty(); // "RWC" 등

        // 1.5. Get Token Balance
        BigDecimal ERCBalance = ethereumService.getTokenBalance(ownerAddress, tokenContractAddress, myWalletCredentials);
        System.out.println("ERC Balance: " + ERCBalance);
        assertThat(ERCBalance).isNotNull(); // 0일 수는 있지만 null이면 안 됨
    }

    // 2. sendToken (Owner -> Recipient) 테스트
    @Test
    @Order(2)
    @DisplayName("Step 2: sendToken (Owner -> Recipient)")
    void testSendToken() throws Exception {
        System.out.println("--- Test: Step 2 (sendToken) ---");
        // given
        recipientBalanceBeforeSend = ethereumService.getTokenBalance(recipientAddress, tokenContractAddress, recipientCredentials);
        System.out.println("Recipient balance (before): " + recipientBalanceBeforeSend);

        // when
        TransactionReceipt receipt = ethereumService.sendToken(tokenContractAddress, myWalletCredentials, recipientAddress, AMOUNT_TO_SEND);

        // then
        assertThat(receipt).isNotNull();
        assertThat(receipt.isStatusOK()).isTrue();
        assertThat(receipt.getFrom()).isEqualToIgnoringCase(ownerAddress);
        System.out.println("sendToken TxHash: " + receipt.getTransactionHash());

        BigDecimal finalBalance = ethereumService.getTokenBalance(recipientAddress, tokenContractAddress, recipientCredentials);
        System.out.println("Recipient balance (after): " + finalBalance);

        // 부동소수점 비교를 위해 scale 설정
        BigDecimal expectedBalance = recipientBalanceBeforeSend.add(AMOUNT_TO_SEND);
        assertThat(finalBalance).isEqualByComparingTo(expectedBalance);
    }

    // 3. approve (Owner가 Recipient/Spender를 승인)
    @Test
    @Order(3)
    @DisplayName("Step 3: approveToken (Owner approves Recipient)")
    void testApproveToken() throws Exception {
        System.out.println("--- Test: Step 3 (approveToken) ---");
        // given
        // Owner(credentials)가 Recipient(recipientAddress)에게 AMOUNT_TO_APPROVE 만큼 승인

        // when
        TransactionReceipt receipt = ethereumService.approveToken(tokenContractAddress, myWalletCredentials, spenderAddress, AMOUNT_TO_APPROVE);

        // then
        assertThat(receipt.getTransactionHash()).isNotNull().startsWith("0x");
        assertThat(receipt.getFrom()).isEqualToIgnoringCase(ownerAddress);
        assertThat(receipt.getTo()).isEqualToIgnoringCase(recipientAddress);
        System.out.println("approveToken TxHash: " + receipt.getTransactionHash());
    }

    // 4. getAllowance (3단계의 승인 내역 확인)
    @Test
    @Order(4)
    @DisplayName("Step 4: getAllowance (Verify approve)")
    void testGetAllowance() throws Exception {
        System.out.println("--- Test: Step 4 (getAllowance) ---");
        // given: 3단계에서 승인 완료

        // when
        BigDecimal allowance = ethereumService.getAllowance(tokenContractAddress, myWalletCredentials, ownerAddress, spenderAddress);

        // then
        System.out.println("Current Allowance: " + allowance);
        assertThat(allowance).isEqualByComparingTo(AMOUNT_TO_APPROVE);
    }

    // 5. transferFrom (Recipient/Spender가 Owner의 토큰을 자신에게 전송)
    @Test
    @Order(5)
    @DisplayName("Step 5: transferTokenFrom (Spender pulls from Owner)")
    void testTransferFrom_Success() throws Exception {
        System.out.println("--- Test: Step 5 (transferTokenFrom) ---");
        // given
        // Spender(spenderCredentials)가 Owner(ownerAddress)로부터
        // Recipient(recipientAddress)에게 (즉, 자기 자신에게) AMOUNT_TO_SEND 만큼 전송
        BigDecimal initialBalance = ethereumService.getTokenBalance(recipientAddress, tokenContractAddress, recipientCredentials);
        System.out.println("Recipient balance (before): " + initialBalance);

        // when
        TransactionReceipt receipt = ethereumService.transferTokenFrom(
                tokenContractAddress,
                spenderCredentials, // Spender의 자격증명
                myWalletCredentials,  // Owner의 자격증명 (getTokenBalance 용)
                ownerAddress,         // Owner 주소 (from)
                recipientAddress,     // Recipient 주소 (to)
                AMOUNT_TO_SEND
        );

        // then
        assertThat(receipt).isNotNull();
        assertThat(receipt.isStatusOK()).isTrue();
        System.out.println("transferTokenFrom TxHash: " + receipt.getTransactionHash());

        BigDecimal finalBalance = ethereumService.getTokenBalance(recipientAddress, tokenContractAddress, recipientCredentials);
        System.out.println("Recipient balance (after): " + finalBalance);

        BigDecimal expectedBalance = initialBalance.add(AMOUNT_TO_SEND);
        assertThat(finalBalance).isEqualByComparingTo(expectedBalance);
    }

    // 6. transferFrom 실패 (한도 초과)
    @Test
    @Order(6)
    @DisplayName("Step 6: transferTokenFrom_Fail_InsufficientAllowance")
    void testTransferFrom_Fail() {
        System.out.println("--- Test: Step 6 (transferFrom Fail) ---");
        // given
        // 3단계에서 100개 승인, 5단계에서 0.1개 사용 (남은 한도: 99.9)
        // 남은 한도보다 1개 많은 100.9개를 시도
        BigDecimal remainingAllowance = AMOUNT_TO_APPROVE.subtract(AMOUNT_TO_SEND);
        BigDecimal invalidAmount = remainingAllowance.add(new BigDecimal("1"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ethereumService.transferTokenFrom(
                    tokenContractAddress,
                    recipientCredentials,
                    myWalletCredentials,
                    ownerAddress,
                    recipientAddress,
                    invalidAmount
            );
        });

        System.out.println("Expected error: " + exception.getMessage());
        assertThat(exception.getMessage()).contains("Amount exceeds allowance");
    }

    // 7. SimpleWallet에 예금 (Owner가 SimpleWallet을 승인 -> Owner가 deposit)
    @Test
    @Order(7)
    @DisplayName("Step 7: depositERC20 to SimpleWallet")
    void testDepositERC20() throws Exception {
        System.out.println("--- Test: Step 7 (depositERC20) ---");
        // given
        // 7.1. Owner(credentials)가 SimpleWallet(simpleWalletAddress)을 승인
        System.out.println("Approving SimpleWallet...");
        ethereumService.approveToken(tokenContractAddress, myWalletCredentials, simpleWalletAddress, AMOUNT_FOR_DEPOSIT);

        // 7.2. 예금 전 SimpleWallet 내부 잔액 확인
        BigDecimal initialWalletBalance = ethereumService.getERC20BalanceFromWallet(simpleWalletAddress, myWalletCredentials, tokenContractAddress);
        System.out.println("SimpleWallet balance (before): " + initialWalletBalance);

        // when
        // Owner(credentials)가 deposit 실행 (msg.sender = Owner)
        TransactionReceipt receipt = ethereumService.depositERC20(simpleWalletAddress, myWalletCredentials, tokenContractAddress, AMOUNT_FOR_DEPOSIT);

        // then
        assertThat(receipt).isNotNull();
        assertThat(receipt.isStatusOK()).isTrue();
        System.out.println("depositERC20 TxHash: " + receipt.getTransactionHash());

        BigDecimal finalWalletBalance = ethereumService.getERC20BalanceFromWallet(simpleWalletAddress, myWalletCredentials, tokenContractAddress);
        System.out.println("SimpleWallet balance (after): " + finalWalletBalance);

        BigDecimal expectedBalance = initialWalletBalance.add(AMOUNT_FOR_DEPOSIT);
        assertThat(finalWalletBalance).isEqualByComparingTo(expectedBalance);

        // 8단계 테스트를 위해 상태 저장
        walletBalanceBeforeWithdraw = finalWalletBalance;
    }

    // 8. SimpleWallet에서 출금 (Owner가 withdraw)
    @Test
    @Order(8)
    @DisplayName("Step 8: withdrawERC20 from SimpleWallet")
    void testWithdrawERC20() throws Exception {
        System.out.println("--- Test: Step 8 (withdrawERC20) ---");
        // given: 7단계에서 10.5개 예금 완료
        BigDecimal initialOwnerBalance = ethereumService.getTokenBalance(ownerAddress, tokenContractAddress, myWalletCredentials);
        System.out.println("Owner balance (before withdraw): " + initialOwnerBalance);
        walletBalanceBeforeWithdraw = ethereumService.getERC20BalanceFromWallet(simpleWalletAddress, myWalletCredentials, tokenContractAddress);
        System.out.println("SimpleWallet balance (before withdraw): " + walletBalanceBeforeWithdraw);

        // when
        TransactionReceipt receipt = ethereumService.withdrawERC20(simpleWalletAddress, myWalletCredentials, tokenContractAddress, AMOUNT_FOR_WITHDRAW);

        // then
        assertThat(receipt).isNotNull();
        assertThat(receipt.isStatusOK()).isTrue();
        System.out.println("withdrawERC20 TxHash: " + receipt.getTransactionHash());

        // 8.1. Owner 토큰 잔액 검증
        BigDecimal finalOwnerBalance = ethereumService.getTokenBalance(ownerAddress, tokenContractAddress, myWalletCredentials);
        System.out.println("Owner balance (after): " + finalOwnerBalance);
        BigDecimal expectedOwnerBalance = initialOwnerBalance.add(AMOUNT_FOR_WITHDRAW);
        assertThat(finalOwnerBalance).isEqualByComparingTo(expectedOwnerBalance);

        // 8.2. SimpleWallet 내부 잔액 검증
        BigDecimal finalWalletBalance = ethereumService.getERC20BalanceFromWallet(simpleWalletAddress, myWalletCredentials, tokenContractAddress);
        System.out.println("SimpleWallet balance (after): " + finalWalletBalance);
        BigDecimal expectedWalletBalance = walletBalanceBeforeWithdraw.subtract(AMOUNT_FOR_WITHDRAW);
        assertThat(finalWalletBalance).isEqualByComparingTo(expectedWalletBalance);
    }
}