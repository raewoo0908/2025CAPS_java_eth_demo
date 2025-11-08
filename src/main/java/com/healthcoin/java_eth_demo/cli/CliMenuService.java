package com.healthcoin.java_eth_demo.cli;

import com.healthcoin.java_eth_demo.contracts.TOGame;
import com.healthcoin.java_eth_demo.service.EthereumService;
import com.healthcoin.java_eth_demo.service.TOGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * CLI 메뉴 처리를 위한 서비스 클래스
 * 사용자 입력을 받아 적절한 비즈니스 로직을 실행합니다.
 */
@Service
public class CliMenuService {
    
    @Autowired
    private TOGameService toGameService;
    
    @Autowired
    private EthereumService ethereumService;
    
    @Value("${togame.contract-address}")
    private String gameContractAddress;
    
    @Value("${erc20.contract-address}")
    private String tokenContractAddress;
    
    private Scanner scanner;
    private Credentials userCredentials;
    
    public CliMenuService() {
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * 사용자 로그인 (Private Key 입력)
     * 환경변수에 설정된 값이 있으면 자동으로 로드, 없으면 수동 입력
     */
    public void login() {
        ConsoleUtils.printSeparator();
        
        // 환경변수에서 Private Key 확인
        String privateKey = System.getenv("PLAYER_PRIVATE_KEY");
        
        if (privateKey != null && !privateKey.isEmpty()) {
            ConsoleUtils.printInfo("환경변수에서 Private Key를 불러왔습니다.");
            try {
                this.userCredentials = Credentials.create(privateKey);
                ConsoleUtils.printSuccess("로그인 성공!");
                ConsoleUtils.printInfo("주소: " + userCredentials.getAddress());
                Thread.sleep(1000);
            } catch (Exception e) {
                ConsoleUtils.printError("환경변수의 Private Key가 올바르지 않습니다: " + e.getMessage());
                System.exit(1);
            }
        } else {
            // 환경변수에 없으면 수동 입력
            ConsoleUtils.printInfo("Private Key를 입력하세요 (0x로 시작):");
            ConsoleUtils.printPrompt("Private Key");
            
            try {
                // Scanner 사용 가능한지 확인
                if (!scanner.hasNextLine()) {
                    ConsoleUtils.printError("입력을 받을 수 없습니다. 환경변수 PLAYER_PRIVATE_KEY를 설정하세요.");
                    ConsoleUtils.printInfo("예: export PLAYER_PRIVATE_KEY=0x...");
                    System.exit(1);
                }
                
                privateKey = scanner.nextLine().trim();
                
                if (privateKey.isEmpty()) {
                    ConsoleUtils.printError("Private Key를 입력해주세요.");
                    System.exit(1);
                }
                
                this.userCredentials = Credentials.create(privateKey);
                ConsoleUtils.printSuccess("로그인 성공!");
                ConsoleUtils.printInfo("주소: " + userCredentials.getAddress());
                Thread.sleep(1000);
            } catch (java.util.NoSuchElementException e) {
                ConsoleUtils.printError("입력을 받을 수 없습니다. 환경변수 PLAYER_PRIVATE_KEY를 설정하세요.");
                ConsoleUtils.printInfo("예: export PLAYER_PRIVATE_KEY=0x...");
                System.exit(1);
            } catch (Exception e) {
                ConsoleUtils.printError("잘못된 Private Key입니다: " + e.getMessage());
                System.exit(1);
            }
        }
    }
    
    /**
     * 1. 게임 참가 (Approve Token)
     * 사용자가 게임에 참가하기 위해 토큰을 approve합니다.
     */
    public void handleJoinGame() {
        try {
            ConsoleUtils.printHeader("게임 참가 (Token Approve)");
            ConsoleUtils.printInfo("게임 컨트랙트에 토큰 사용 권한을 부여합니다.");
            ConsoleUtils.printInfo("게임 컨트랙트: " + gameContractAddress);
            ConsoleUtils.printInfo("토큰 컨트랙트: " + tokenContractAddress);
            
            ConsoleUtils.printPrompt("Approve할 토큰 양 (ETH 단위, 예: 100)");
            BigDecimal amount = scanner.nextBigDecimal();
            scanner.nextLine(); // 버퍼 비우기
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                ConsoleUtils.printError("금액은 0보다 커야 합니다.");
                return;
            }
            
            ConsoleUtils.printLoading("트랜잭션 전송 중...");
            
            var receipt = ethereumService.approveToken(
                tokenContractAddress,
                userCredentials,
                gameContractAddress,
                amount
            );
            
            ConsoleUtils.printTransactionResult(
                receipt.getTransactionHash(),
                receipt.getGasUsed().toString()
            );
            
        } catch (Exception e) {
            ConsoleUtils.printError("Approve 실패: " + e.getMessage());
        }
    }
    
    /**
     * 2. 숫자 제출 (Submit Number)
     * 사용자가 1, 2, 3 중 하나의 숫자를 제출합니다.
     */
    public void handleSubmitNumber() {
        try {
            ConsoleUtils.printHeader("숫자 제출 (Submit Number)");
            
            // 현재 라운드 조회
            BigInteger currentRound = toGameService.getCurrentRound(userCredentials);
            ConsoleUtils.printInfo("현재 라운드: " + currentRound);
            
            // 숫자 입력 (1, 2, 3 검증)
            int number;
            while (true) {
                ConsoleUtils.printPrompt("제출할 숫자 (1, 2, 또는 3)");
                try {
                    number = scanner.nextInt();
                    scanner.nextLine();
                    
                    if (number >= 1 && number <= 3) {
                        break;
                    }
                    ConsoleUtils.printError("1, 2, 3 중 하나를 입력해주세요.");
                } catch (Exception e) {
                    ConsoleUtils.printError("유효한 숫자를 입력해주세요.");
                    scanner.nextLine(); // 버퍼 비우기
                }
            }
            
            // 베팅 금액 입력 (ETH 단위로 입력받아 Wei로 변환)
            ConsoleUtils.printPrompt("베팅 금액 (ETH 단위, 예: 10)");
            BigDecimal amountInEth = scanner.nextBigDecimal();
            scanner.nextLine();
            
            if (amountInEth.compareTo(BigDecimal.ZERO) <= 0) {
                ConsoleUtils.printError("금액은 0보다 커야 합니다.");
                return;
            }
            
            // Wei로 변환
            BigInteger amountInWei = Convert.toWei(amountInEth, Convert.Unit.ETHER).toBigInteger();
            ConsoleUtils.printInfo("베팅: " + amountInEth + " ETH = " + amountInWei + " Wei");
            
            ConsoleUtils.printLoading("트랜잭션 전송 중...");
            
            var receipt = toGameService.submitNumber(
                userCredentials,
                currentRound,
                BigInteger.valueOf(number),
                amountInWei
            );
            
            ConsoleUtils.printTransactionResult(
                receipt.getTransactionHash(),
                receipt.getGasUsed().toString()
            );
            
        } catch (Exception e) {
            ConsoleUtils.printError("숫자 제출 실패: " + e.getMessage());
        }
    }
    
    /**
     * 3. 새 라운드 시작
     * 현재 라운드가 종료된 경우 새 라운드를 시작합니다.
     */
    public void handleStartNewRound() {
        try {
            ConsoleUtils.printHeader("새 라운드 시작");
            
            // 현재 라운드 정보 확인
            BigInteger currentRound = toGameService.getCurrentRound(userCredentials);
            Map<String, Object> info = toGameService.getRoundInfo(userCredentials, currentRound);
            
            ConsoleUtils.printInfo("현재 라운드: " + currentRound);
            ConsoleUtils.printInfo("게임 종료 여부: " + info.get("isGameOver"));
            
            if (Boolean.FALSE.equals(info.get("isGameOver"))) {
                ConsoleUtils.printWarning("현재 라운드가 아직 진행 중입니다.");
                ConsoleUtils.printPrompt("그래도 시작하시겠습니까? (y/n)");
            } else {
                ConsoleUtils.printPrompt("새 라운드를 시작하시겠습니까? (y/n)");
            }
            
            String confirm = scanner.nextLine().trim();
            
            if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
                ConsoleUtils.printLoading("트랜잭션 전송 중...");
                var receipt = toGameService.startNewRound(userCredentials);
                
                ConsoleUtils.printTransactionResult(
                    receipt.getTransactionHash(),
                    receipt.getGasUsed().toString()
                );
                ConsoleUtils.printSuccess("새 라운드가 시작되었습니다!");
            } else {
                ConsoleUtils.printInfo("취소되었습니다.");
            }
            
        } catch (Exception e) {
            ConsoleUtils.printError("라운드 시작 실패: " + e.getMessage());
        }
    }
    
    /**
     * 4. 현재 라운드 정보 조회
     * 특정 라운드의 상세 정보를 조회합니다.
     */
    public void handleGetRoundInfo() {
        try {
            ConsoleUtils.printHeader("라운드 정보 조회");
            
            BigInteger currentRound = toGameService.getCurrentRound(userCredentials);
            ConsoleUtils.printInfo("현재 라운드: " + currentRound);
            
            ConsoleUtils.printPrompt("조회할 라운드 번호 [Enter=현재 라운드]");
            String input = scanner.nextLine().trim();
            
            BigInteger roundId = input.isEmpty() ? currentRound : new BigInteger(input);
            
            var info = toGameService.getRoundInfo(userCredentials, roundId);
            
            ConsoleUtils.printSeparator();
            System.out.println(ConsoleUtils.CYAN + ConsoleUtils.BOLD + "📊 라운드 #" + roundId + " 정보:" + ConsoleUtils.RESET);
            System.out.println();
            System.out.println("  " + ConsoleUtils.BLUE + "현재 인덱스:" + ConsoleUtils.RESET + " " + info.get("currentIndex"));
            
            BigInteger prizePool = (BigInteger) info.get("prizePool");
            BigDecimal prizeInEth = Convert.fromWei(new BigDecimal(prizePool), Convert.Unit.ETHER);
            System.out.println("  " + ConsoleUtils.BLUE + "상금 풀:" + ConsoleUtils.RESET + " " + prizePool + " Wei (약 " + prizeInEth + " ETH)");
            
            System.out.println("  " + ConsoleUtils.BLUE + "게임 종료:" + ConsoleUtils.RESET + " " + info.get("isGameOver"));
            System.out.println("  " + ConsoleUtils.BLUE + "승자 비율:" + ConsoleUtils.RESET + " " + info.get("winnerPercentage") + "%");
            ConsoleUtils.printSeparator();
            
        } catch (Exception e) {
            ConsoleUtils.printError("조회 실패: " + e.getMessage());
        }
    }
    
    /**
     * 5. 플레이어 목록 조회
     * 특정 라운드에 참가한 플레이어 목록을 조회합니다.
     */
    public void handleGetPlayers() {
        try {
            ConsoleUtils.printHeader("플레이어 목록 조회");
            
            BigInteger currentRound = toGameService.getCurrentRound(userCredentials);
            ConsoleUtils.printInfo("현재 라운드: " + currentRound);
            
            ConsoleUtils.printPrompt("조회할 라운드 번호 [Enter=현재 라운드]");
            String input = scanner.nextLine().trim();
            
            BigInteger roundId = input.isEmpty() ? currentRound : new BigInteger(input);
            
            List<TOGame.Player> players = toGameService.getRoundPlayers(userCredentials, roundId);
            
            ConsoleUtils.printSeparator();
            System.out.println(ConsoleUtils.CYAN + ConsoleUtils.BOLD + "👥 라운드 #" + roundId + " 플레이어 목록 (총 " + players.size() + "명):" + ConsoleUtils.RESET);
            System.out.println();
            
            if (players.isEmpty()) {
                ConsoleUtils.printWarning("참가한 플레이어가 없습니다.");
            } else {
                for (int i = 0; i < players.size(); i++) {
                    TOGame.Player player = players.get(i);
                    BigDecimal amountInEth = Convert.fromWei(new BigDecimal(player.amount), Convert.Unit.ETHER);
                    
                    System.out.println("  " + ConsoleUtils.YELLOW + "[" + (i+1) + "]" + ConsoleUtils.RESET);
                    System.out.println("      주소: " + player.playerAddress);
                    System.out.println("      베팅액: " + player.amount + " Wei (약 " + amountInEth + " ETH)");
                    System.out.println();
                }
                ConsoleUtils.printInfo("💡 제출한 숫자는 컨트랙트 이벤트 로그에서 확인 가능합니다.");
            }
            ConsoleUtils.printSeparator();
            
        } catch (Exception e) {
            ConsoleUtils.printError("조회 실패: " + e.getMessage());
        }
    }
    
    /**
     * 6. 승자 조회
     * 특정 라운드의 승자를 조회합니다.
     */
    public void handleGetWinner() {
        try {
            ConsoleUtils.printHeader("승자 조회");
            
            BigInteger currentRound = toGameService.getCurrentRound(userCredentials);
            ConsoleUtils.printInfo("현재 라운드: " + currentRound);
            
            ConsoleUtils.printPrompt("조회할 라운드 번호 [Enter=현재 라운드]");
            String input = scanner.nextLine().trim();
            
            BigInteger roundId = input.isEmpty() ? currentRound : new BigInteger(input);
            
            String winner = toGameService.getWinner(userCredentials, roundId);
            
            ConsoleUtils.printSeparator();
            if (winner.equals("0x0000000000000000000000000000000000000000")) {
                ConsoleUtils.printWarning("라운드 #" + roundId + ": 아직 승자가 결정되지 않았습니다.");
            } else {
                System.out.println(ConsoleUtils.GREEN + ConsoleUtils.BOLD + "🏆 라운드 #" + roundId + " 승자:" + ConsoleUtils.RESET);
                System.out.println("   " + winner);
                
                // 자신이 승자인지 확인
                if (winner.equalsIgnoreCase(userCredentials.getAddress())) {
                    System.out.println(ConsoleUtils.GREEN + ConsoleUtils.BOLD + "\n   🎉 축하합니다! 당신이 승자입니다! 🎉" + ConsoleUtils.RESET);
                }
            }
            ConsoleUtils.printSeparator();
            
        } catch (Exception e) {
            ConsoleUtils.printError("조회 실패: " + e.getMessage());
        }
    }
    
    /**
     * 7. 컨트랙트 잔액 조회
     * 게임 컨트랙트가 보유한 토큰 잔액을 조회합니다.
     */
    public void handleGetContractBalance() {
        try {
            ConsoleUtils.printHeader("컨트랙트 잔액 조회");
            
            BigInteger balance = toGameService.getContractBalance(userCredentials);
            BigDecimal balanceInEth = Convert.fromWei(new BigDecimal(balance), Convert.Unit.ETHER);
            
            ConsoleUtils.printSeparator();
            System.out.println(ConsoleUtils.GREEN + "💰 게임 컨트랙트 토큰 잔액:" + ConsoleUtils.RESET);
            System.out.println("   " + balance + " Wei");
            System.out.println("   (약 " + balanceInEth + " ETH)");
            ConsoleUtils.printSeparator();
            
        } catch (Exception e) {
            ConsoleUtils.printError("조회 실패: " + e.getMessage());
        }
    }
    
    /**
     * 8. 내 토큰 잔액 조회
     * 로그인한 사용자의 토큰 잔액을 조회합니다.
     */
    public void handleGetMyBalance() {
        try {
            ConsoleUtils.printHeader("내 토큰 잔액 조회");
            
            BigDecimal balance = ethereumService.getTokenBalance(
                userCredentials.getAddress(),
                tokenContractAddress,
                userCredentials
            );
            
            ConsoleUtils.printSeparator();
            System.out.println(ConsoleUtils.GREEN + "💳 내 토큰 잔액:" + ConsoleUtils.RESET);
            System.out.println("   주소: " + userCredentials.getAddress());
            System.out.println("   잔액: " + balance + " ETH");
            ConsoleUtils.printSeparator();
            
        } catch (Exception e) {
            ConsoleUtils.printError("조회 실패: " + e.getMessage());
        }
    }
    
    /**
     * 9. Allowance 조회
     * 게임 컨트랙트가 사용할 수 있는 토큰 양을 조회합니다.
     */
    public void handleGetAllowance() {
        try {
            ConsoleUtils.printHeader("Allowance 조회");
            
            BigDecimal allowance = ethereumService.getAllowance(
                tokenContractAddress,
                userCredentials,
                userCredentials.getAddress(),
                gameContractAddress
            );
            
            ConsoleUtils.printSeparator();
            System.out.println(ConsoleUtils.CYAN + "✓ Approve된 금액:" + ConsoleUtils.RESET);
            System.out.println("   소유자: " + userCredentials.getAddress());
            System.out.println("   사용자: " + gameContractAddress);
            System.out.println("   금액: " + allowance + " ETH");
            
            if (allowance.compareTo(BigDecimal.ZERO) <= 0) {
                ConsoleUtils.printWarning("\n⚠ Approve된 금액이 없습니다. 메뉴 1번을 선택하여 Approve를 먼저 진행해주세요.");
            }
            ConsoleUtils.printSeparator();
            
        } catch (Exception e) {
            ConsoleUtils.printError("조회 실패: " + e.getMessage());
        }
    }
}

