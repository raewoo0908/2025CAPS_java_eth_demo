package com.healthcoin.java_eth_demo.cli;

/**
 * 콘솔 출력을 위한 유틸리티 클래스
 * ANSI 색상 코드를 사용하여 가독성 높은 CLI 인터페이스를 제공합니다.
 */
public class ConsoleUtils {
    
    // ANSI 색상 코드
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    
    // ANSI 배경색 코드
    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_MAGENTA = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";
    
    // 텍스트 스타일
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";
    
    /**
     * 메인 메뉴 출력
     */
    public static void printMainMenu() {
        System.out.println("\n" + CYAN + BOLD + "╔═══════════════════════════════════════╗");
        System.out.println("║       31 GAME - CLI INTERFACE        ║");
        System.out.println("╚═══════════════════════════════════════╝" + RESET);
        System.out.println(BLUE + "┌─────────────────────────────────────┐");
        System.out.println("│  1. 게임 참가 (Token Approve)       │");
        System.out.println("│  2. 숫자 제출 (Submit Number)       │");
        System.out.println("│  3. 새 라운드 시작                   │");
        System.out.println("│  4. 현재 라운드 정보 조회            │");
        System.out.println("│  5. 플레이어 목록 조회               │");
        System.out.println("│  6. 승자 조회                        │");
        System.out.println("│  7. 컨트랙트 잔액 조회               │");
        System.out.println("│  8. 내 토큰 잔액 조회                │");
        System.out.println("│  9. Allowance 조회                   │");
        System.out.println("│  0. 종료                             │");
        System.out.println("└─────────────────────────────────────┘" + RESET);
        System.out.print(YELLOW + BOLD + "선택> " + RESET);
    }
    
    /**
     * 성공 메시지 출력
     * @param message 출력할 메시지
     */
    public static void printSuccess(String message) {
        System.out.println(GREEN + "✓ " + message + RESET);
    }
    
    /**
     * 에러 메시지 출력
     * @param message 출력할 에러 메시지
     */
    public static void printError(String message) {
        System.out.println(RED + "✗ " + message + RESET);
    }
    
    /**
     * 정보 메시지 출력
     * @param message 출력할 정보 메시지
     */
    public static void printInfo(String message) {
        System.out.println(BLUE + "ℹ " + message + RESET);
    }
    
    /**
     * 경고 메시지 출력
     * @param message 출력할 경고 메시지
     */
    public static void printWarning(String message) {
        System.out.println(YELLOW + "⚠ " + message + RESET);
    }
    
    /**
     * 구분선 출력
     */
    public static void printSeparator() {
        System.out.println(CYAN + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
    }
    
    /**
     * 두꺼운 구분선 출력
     */
    public static void printThickSeparator() {
        System.out.println(CYAN + "════════════════════════════════════════" + RESET);
    }
    
    /**
     * 화면 정리 (Clear Screen)
     */
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    /**
     * 헤더 출력 (중요한 섹션 제목용)
     * @param title 헤더 제목
     */
    public static void printHeader(String title) {
        System.out.println("\n" + CYAN + BOLD + "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
        System.out.println("┃  " + title);
        System.out.println("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛" + RESET);
    }
    
    /**
     * 트랜잭션 결과 출력
     * @param txHash 트랜잭션 해시
     * @param gasUsed 사용된 가스
     */
    public static void printTransactionResult(String txHash, String gasUsed) {
        printSeparator();
        printSuccess("트랜잭션 성공!");
        System.out.println(CYAN + "  📝 트랜잭션 해시: " + RESET + txHash);
        System.out.println(CYAN + "  ⛽ Gas 사용량: " + RESET + gasUsed);
        printSeparator();
    }
    
    /**
     * 로딩 중 메시지 출력
     * @param message 로딩 메시지
     */
    public static void printLoading(String message) {
        System.out.println(YELLOW + "⏳ " + message + RESET);
    }
    
    /**
     * 환영 메시지 출력
     */
    public static void printWelcome() {
        clearScreen();
        System.out.println(CYAN + BOLD + """
        ╔═══════════════════════════════════════════════════════╗
        ║                                                       ║
        ║          🎮 31 GAME CLI INTERFACE 🎮                 ║
        ║                                                       ║
        ║          블록체인 기반 31게임에 오신 것을 환영합니다!   ║
        ║                                                       ║
        ╚═══════════════════════════════════════════════════════╝
        """ + RESET);
    }
    
    /**
     * 종료 메시지 출력
     */
    public static void printGoodbye() {
        printSeparator();
        System.out.println(CYAN + BOLD + "\n  👋 31 Game CLI를 종료합니다. 안녕히 가세요!\n" + RESET);
        printSeparator();
    }
    
    /**
     * 입력 프롬프트 출력
     * @param prompt 프롬프트 메시지
     */
    public static void printPrompt(String prompt) {
        System.out.print(YELLOW + prompt + " > " + RESET);
    }
    
    /**
     * 계속하기 프롬프트 출력
     */
    public static void printContinuePrompt() {
        System.out.println(MAGENTA + "\n계속하려면 Enter를 누르세요..." + RESET);
    }
}

