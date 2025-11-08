package com.healthcoin.java_eth_demo.cli;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * CLI 모드 실행을 위한 CommandLineRunner 구현체
 * app.mode=cli로 설정된 경우에만 실행됩니다.
 */
@Component
@ConditionalOnProperty(name = "app.mode", havingValue = "cli")
public class TOGameCliRunner implements CommandLineRunner {
    
    @Autowired
    private CliMenuService menuService;
    
    private Scanner scanner;
    
    public TOGameCliRunner() {
        this.scanner = new Scanner(System.in);
    }
    
    @Override
    public void run(String... args) throws Exception {
        // 환영 메시지 출력
        ConsoleUtils.printWelcome();
        
        // 로그인 (Private Key 입력 또는 환경변수에서 로드)
        menuService.login();
        
        // 메인 루프
        boolean running = true;
        while (running) {
            try {
                // 메뉴 표시
                ConsoleUtils.printMainMenu();
                
                // 사용자 선택 입력
                int choice;
                try {
                    choice = scanner.nextInt();
                    scanner.nextLine(); // 버퍼 비우기
                } catch (Exception e) {
                    ConsoleUtils.printError("유효한 숫자를 입력해주세요.");
                    scanner.nextLine(); // 버퍼 비우기
                    continue;
                }
                
                // 선택에 따른 메뉴 처리
                switch (choice) {
                    case 1:
                        menuService.handleJoinGame();
                        break;
                    case 2:
                        menuService.handleSubmitNumber();
                        break;
                    case 3:
                        menuService.handleStartNewRound();
                        break;
                    case 4:
                        menuService.handleGetRoundInfo();
                        break;
                    case 5:
                        menuService.handleGetPlayers();
                        break;
                    case 6:
                        menuService.handleGetWinner();
                        break;
                    case 7:
                        menuService.handleGetContractBalance();
                        break;
                    case 8:
                        menuService.handleGetMyBalance();
                        break;
                    case 9:
                        menuService.handleGetAllowance();
                        break;
                    case 0:
                        ConsoleUtils.printGoodbye();
                        running = false;
                        break;
                    default:
                        ConsoleUtils.printWarning("잘못된 선택입니다. 0-9 사이의 숫자를 입력해주세요.");
                }
                
                // 계속하기 프롬프트 (종료가 아닌 경우)
                if (running && choice != 0) {
                    ConsoleUtils.printContinuePrompt();
                    scanner.nextLine();
                }
                
            } catch (Exception e) {
                ConsoleUtils.printError("오류 발생: " + e.getMessage());
                e.printStackTrace();
                
                // 버퍼 정리
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }
                
                // 계속 진행할지 확인
                ConsoleUtils.printWarning("계속 진행하시겠습니까? (y/n)");
                String continueChoice = scanner.nextLine().trim();
                if (!continueChoice.equalsIgnoreCase("y") && !continueChoice.equalsIgnoreCase("yes")) {
                    ConsoleUtils.printGoodbye();
                    running = false;
                }
            }
        }
        
        // 스캐너 종료
        scanner.close();
        
        // 프로그램 종료
        System.exit(0);
    }
}

