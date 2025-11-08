package com.healthcoin.java_eth_demo.service;

// 1. 생성된 Contract Wrapper 임포트
import com.healthcoin.java_eth_demo.contracts.TOGame;

// 2. Spring 및 Web3j 필수 라이브러리 임포트
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tuples.generated.Tuple4; // getRoundInfo 반환 타입

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Service
public class TOGameService {

    // Web3j 연결 객체 (Config에서 주입)
    @Autowired
    private Web3j web3j;

    // setWinnerPercentage 등 'onlyOwner' 함수 호출 시 사용
    @Autowired
    private Credentials myWalletCredentials;

    // application.properties에서 컨트랙트 주소 주입
    @Value("${togame.contract-address}")
    private String gameContractAddress;

    // 트랜잭션 가스비 공급자
    private final ContractGasProvider gasProvider = new DefaultGasProvider();

    // --- Helper Method ---

    /**
     * 특정 사용자의 Credentials로 컨트랙트 래퍼를 로드합니다.
     * (트랜잭션 서명자가 중요할 때 사용)
     */
    private TOGame loadContract(Credentials credentials) {
        return TOGame.load(gameContractAddress, web3j, credentials, gasProvider);
    }

    // --- WRITE (State-Changing) Methods ---

    /**
     * submit: 플레이어가 숫자를 제출합니다.
     * (플레이어의 Credentials로 서명해야 함)
     *
     * @param playerCredentials 숫자를 제출하는 플레이어의 Credentials
     * @param round             현재 라운드 번호
     * @param number            제출할 숫자 (1, 2, 또는 3)
     * @param amountInWei            베팅할 토큰의 양 (Wei 단위)
     * @return 트랜잭션 영수증
     */
    public TransactionReceipt submitNumber(Credentials playerCredentials, BigInteger round, BigInteger number, BigInteger amountInWei) throws Exception {

        // round exception handling
        if (!getCurrentRound(playerCredentials).equals(round)){
            throw new RuntimeException("Current round is " + getCurrentRound(playerCredentials) + ", argument round is " + round);
        }

        // number exception handling
        if (number.compareTo(BigInteger.valueOf(1)) == -1 || number.compareTo(BigInteger.valueOf(3)) == 1){
            throw new RuntimeException("Number should be between 1 and 3, submitted number: " + number);
        }

        // amount exception handling
        if (amountInWei.compareTo(BigInteger.valueOf(10).pow(18)) <= 0 || amountInWei.compareTo(BigInteger.valueOf(50).pow(18)) == 1){
            throw new RuntimeException("Amount should be between 10 and 50, submitted amount: " + amountInWei);
        }

        TOGame gameAsPlayer = loadContract(playerCredentials);
        return gameAsPlayer.submit(round, number, amountInWei).send();
    }

    /**
     * startNewRound: 새 라운드를 시작합니다.
     * @param callerCredentials 함수를 호출하는 모든 사용자의 Credentials
     * @return 트랜잭션 영수증
     */
    public TransactionReceipt startNewRound(Credentials callerCredentials) throws Exception {

        if (getRoundInfo(callerCredentials, getCurrentRound(callerCredentials)).get("isGameOver").equals(false)) {
            throw new RuntimeException("Current round is not over.");
        }

        TOGame game = loadContract(callerCredentials);
        return game.startNewRound().send();
    }

    /**
     * 3. setWinnerPercentage: 승자 상금 비율을 설정합니다. (Owner 전용)
     * (서버 소유자의 Credentials로 자동 서명)
     *
     * @param newPercentage 새로운 비율 (1-100)
     * @return 트랜잭션 영수증
     */
    public TransactionReceipt setWinnerPercentage(Credentials callerCredentials, BigInteger newPercentage) throws Exception {
        if (!(callerCredentials.getAddress().toLowerCase().equals(getOwner(callerCredentials).toLowerCase()))){
            throw new RuntimeException("This method should only be called by Owner of the contract.");
        }

        TOGame gameAsOwner = loadContract(callerCredentials);
        return gameAsOwner.setWinnerPercentage(newPercentage).send();
    }

    // --- VIEW (Read-Only) Methods ---

    /**
     * Get round informations. currentIndex, prizePool, isGameOver, winnerPercentage.
     * @param playerCredentials credentials for load contract
     * @param roundId           round id
     * @return                  Map<String, Object> object
     * @throws Exception
     */
    public Map<String, Object> getRoundInfo(Credentials playerCredentials, BigInteger roundId) throws Exception {
        Tuple4 roundInfo = loadContract(playerCredentials).getRoundInfo(roundId).send();
        Map<String, Object> result = Map.of(
                "currentIndex", roundInfo.component1(),
                "prizePool", roundInfo.component2(),
                "isGameOver", roundInfo.component3(),
                "winnerPercentage", roundInfo.component4());

        return result;
    }

    /**
     * getRoundPlayers: 특정 라운드의 플레이어 목록과 총 베팅액을 조회합니다.
     * (ThirtyOneGame.Player는 래퍼 생성 시 자동으로 생성된 static class입니다.)
     *
     * @param roundId 조회할 라운드 ID
     * @return 플레이어 목록
     */
    public List<TOGame.Player> getRoundPlayers(Credentials playerCredentials, BigInteger roundId) throws Exception {
        return loadContract(playerCredentials).getRoundPlayers(roundId).send();
    }

    /**
     * token: 게임 토큰(ERC20)의 컨트랙트 주소를 반환합니다.
     */
    public String getTokenAddress(Credentials playerCredentials) throws Exception {
        return loadContract(playerCredentials).token().send();
    }

    /**
     * currentRound: 현재 진행 중인 라운드 번호를 반환합니다.
     */
    public BigInteger getCurrentRound(Credentials playerCredentials) throws Exception {
        return loadContract(playerCredentials).currentRound().send();
    }

    /**
     * winners: 특정 라운드의 승자 주소를 반환합니다.
     */
    public String getWinner(Credentials playerCredentials, BigInteger roundId) throws Exception {
        return loadContract(playerCredentials).winners(roundId).send();
    }

    /**
     * winnerPercentage: 현재 설정된 기본 승자 비율을 반환합니다.
     */
    public BigInteger getWinnerPercentage(Credentials playerCredentials) throws Exception {
        return loadContract(playerCredentials).winnerPercentage().send();
    }

    /**
     * owner: 이 컨트랙트의 소유자(Owner) 주소를 반환합니다.
     */
    public String getOwner(Credentials playerCredentials) throws Exception {
        return loadContract(playerCredentials).owner().send();
    }

    /**
     * getContractBalance: 컨트랙트가 보유한 게임 토큰의 총 잔액을 반환합니다.
     */
    public BigInteger getContractBalance(Credentials playerCredentials) throws Exception {
        return loadContract(playerCredentials).getContractBalance().send();
    }
}