package com.healthcoin.java_eth_demo.controller;

import com.healthcoin.java_eth_demo.DTO.Request.*;
import com.healthcoin.java_eth_demo.DTO.Response.PlayerDTO;
import com.healthcoin.java_eth_demo.DTO.Response.RoundInfoDTO;
import com.healthcoin.java_eth_demo.DTO.Response.TxReceiptDTO;
import com.healthcoin.java_eth_demo.DTO.Response.ValueDTO;
import com.healthcoin.java_eth_demo.contracts.TOGame;
import com.healthcoin.java_eth_demo.service.EthereumService;
import com.healthcoin.java_eth_demo.service.TOGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/31game")
public class TOGameController {
    @Autowired
    private TOGameService toGameService;
    @Autowired
    private EthereumService ethereumService;
    private SetPercentageRequest request;

    // --- Helper Method ---
    /**
     * (INSECURE) 요청 DTO에서 개인 키를 받아 Credentials 객체를 생성합니다.
     */
    private Credentials createCredentials(String privateKey) {
        if (privateKey == null || privateKey.isEmpty()) {
            throw new IllegalArgumentException("Private key must not be null or empty.");
        }
        return Credentials.create(privateKey);
    }

    // --- Approve ---
    @PostMapping("/join")
    public ResponseEntity joinGame(@RequestBody JoinRequest request) throws Exception{
        Credentials playerCredentials = createCredentials(request.getPrivateKey());
        TransactionReceipt receipt = ethereumService.approveToken(
                request.getTokenContractAddress(),
                playerCredentials,
                request.getSpenderAddress(),
                request.getAmountInETH());

        return ResponseEntity.ok(TxReceiptDTO.fromReceipt(receipt));
    }


    // --- WRITE (State-Changing) Endpoints ---

    /**
     * 1. submit: 플레이어가 숫자를 제출합니다.
     */
    @PostMapping("/submit")
    public ResponseEntity<TxReceiptDTO> submitNumber(@RequestBody SubmitRequest request) throws Exception {
        Credentials playerCredentials = createCredentials(request.getPrivateKey());
        TransactionReceipt receipt = toGameService.submitNumber(
                playerCredentials,
                request.getRound(),
                request.getNumber(),
                request.getAmountInWei()
        );
        return ResponseEntity.ok(TxReceiptDTO.fromReceipt(receipt));
    }

    /**
     * 2. startNewRound: 새 라운드를 시작합니다.
     */
    @PostMapping("/start-new-round")
    public ResponseEntity<TxReceiptDTO> startNewRound(@RequestBody PrivateKeyRequest request) throws Exception {
        Credentials callerCredentials = createCredentials(request.getPrivateKey());
        TransactionReceipt receipt = toGameService.startNewRound(callerCredentials);
        return ResponseEntity.ok(TxReceiptDTO.fromReceipt(receipt));
    }

    /**
     * 3. setWinnerPercentage: 승자 상금 비율을 설정합니다. (Owner 전용)
     */
    @PostMapping("/admin/set-percentage")
    public ResponseEntity<TxReceiptDTO> setWinnerPercentage(@RequestBody SetPercentageRequest request) throws Exception {
        this.request = request;
        Credentials ownerCredentials = createCredentials(request.getPrivateKey());
        TransactionReceipt receipt = toGameService.setWinnerPercentage(
                ownerCredentials,
                request.getNewPercentage()
        );
        return ResponseEntity.ok(TxReceiptDTO.fromReceipt(receipt));
    }

    // --- VIEW (Read-Only) Endpoints ---
    // (보안상 Private Key를 Body로 받기 위해 모두 POST로 변경)

    /**
     * 4. getRoundInfo: 특정 라운드의 정보를 조회합니다.
     */
    @PostMapping("/round-info")
    public ResponseEntity<RoundInfoDTO> getRoundInfo(@RequestBody RoundRequest request) throws Exception {
        Credentials credentials = createCredentials(request.getPrivateKey());
        Map<String, Object> info = toGameService.getRoundInfo(credentials, request.getRoundId());
        return ResponseEntity.ok(RoundInfoDTO.fromMap(info));
    }

    /**
     * 5. getRoundPlayers: 특정 라운드의 플레이어 목록을 조회합니다.
     */
    @PostMapping("/round-players")
    public ResponseEntity<List<PlayerDTO>> getRoundPlayers(@RequestBody RoundRequest request) throws Exception {
        Credentials credentials = createCredentials(request.getPrivateKey());
        List<TOGame.Player> players = toGameService.getRoundPlayers(credentials, request.getRoundId());

        List<PlayerDTO> playerDtos = players.stream()
                .map(PlayerDTO::fromPlayer)
                .collect(Collectors.toList());

        return ResponseEntity.ok(playerDtos);
    }

    /**
     * 6. token: 게임 토큰(ERC20)의 컨트랙트 주소를 반환합니다.
     */
    @PostMapping("/token-address")
    public ResponseEntity<ValueDTO<String>> getTokenAddress(@RequestBody PrivateKeyRequest request) throws Exception {
        Credentials credentials = createCredentials(request.getPrivateKey());
        String address = toGameService.getTokenAddress(credentials);
        return ResponseEntity.ok(new ValueDTO<>(address));
    }

    /**
     * 7. currentRound: 현재 진행 중인 라운드 번호를 반환합니다.
     */
    @PostMapping("/current-round")
    public ResponseEntity<ValueDTO<BigInteger>> getCurrentRound(@RequestBody PrivateKeyRequest request) throws Exception {
        Credentials credentials = createCredentials(request.getPrivateKey());
        BigInteger round = toGameService.getCurrentRound(credentials);
        return ResponseEntity.ok(new ValueDTO<>(round));
    }

    /**
     * 8. winners: 특정 라운드의 승자 주소를 반환합니다.
     */
    @PostMapping("/winner")
    public ResponseEntity<ValueDTO<String>> getWinner(@RequestBody RoundRequest request) throws Exception {
        Credentials credentials = createCredentials(request.getPrivateKey());
        String winner = toGameService.getWinner(credentials, request.getRoundId());
        return ResponseEntity.ok(new ValueDTO<>(winner));
    }

    /**
     * 9. winnerPercentage: 현재 설정된 기본 승자 비율을 반환합니다.
     */
    @PostMapping("/winner-percentage")
    public ResponseEntity<ValueDTO<BigInteger>> getWinnerPercentage(@RequestBody PrivateKeyRequest request) throws Exception {
        Credentials credentials = createCredentials(request.getPrivateKey());
        BigInteger percentage = toGameService.getWinnerPercentage(credentials);
        return ResponseEntity.ok(new ValueDTO<>(percentage));
    }

    /**
     * 10. owner: 이 컨트랙트의 소유자(Owner) 주소를 반환합니다.
     */
    @PostMapping("/owner")
    public ResponseEntity<ValueDTO<String>> getOwner(@RequestBody PrivateKeyRequest request) throws Exception {
        Credentials credentials = createCredentials(request.getPrivateKey());
        String owner = toGameService.getOwner(credentials);
        return ResponseEntity.ok(new ValueDTO<>(owner));
    }

    /**
     * 11. getContractBalance: 컨트랙트가 보유한 게임 토큰의 총 잔액을 반환합니다.
     */
    @PostMapping("/contract-balance")
    public ResponseEntity<ValueDTO<BigInteger>> getContractBalance(@RequestBody PrivateKeyRequest request) throws Exception {
        Credentials credentials = createCredentials(request.getPrivateKey());
        BigInteger balance = toGameService.getContractBalance(credentials);
        return ResponseEntity.ok(new ValueDTO<>(balance));
    }

    // --- Global Exception Handler ---
    /**
     * 서비스 레벨에서 발생한 RuntimeException을 처리하여 400 Bad Request로 반환합니다.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        // (예: "Current round is not over.")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    /**
     * 기타 모든 예외를 처리하여 500 Internal Server Error로 반환합니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

}
