package com.officemanagement.main;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import com.officemanagement.dao.ReservationDAO;
import com.officemanagement.dao.UserDAO;
import com.officemanagement.dao.SettingDAO;
import com.officemanagement.model.ReservationDTO;
import com.officemanagement.model.UserDTO;



public class MainApp {
    
    // 전역변수 선언
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserDAO userDAO = new UserDAO();
    private static final SettingDAO settingDAO = new SettingDAO();

    public static void main(String[] args) {
        boolean isRunning = true;

        while (isRunning) {
            printMainMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    handleLogin(); // 로그인 메서드 호출
                    break;
                case "2":
                    handleRegister(); // 회원가입 메서드 호출
                    break;
                case "3":
                    System.out.println("프로그램을 종료합니다.");
                    isRunning = false; // 종료
                    break;
                default:
                    System.out.println("[오류] 올바른 번호를 선택해주세요.");
            }
        }
        scanner.close();
    }

    // 메인 메뉴 출력 메서드
    private static void printMainMenu() {
        System.out.println("\n==============================");
        System.out.println("      공유 오피스 관리 프로그램");
        System.out.println("==============================");
        System.out.println("[1] 로그인");
        System.out.println("[2] 회원가입");
        System.out.println("[3] 프로그램 종료");
        System.out.print("메뉴 선택: ");
    }

    // 로그인 처리 메서드
    private static void handleLogin() {
        System.out.print("아이디: ");
        String username = scanner.nextLine();
        System.out.print("비밀번호: ");
        String password = scanner.nextLine();

        UserDTO loginUser = userDAO.login(username, password);

        if (loginUser != null) {
            System.out.println("[로그인 성공] 환영합니다, " + loginUser.getUsername() + "님!");
            
            // 권한에 따른 메뉴 분기
            if ("ADMIN".equalsIgnoreCase(loginUser.getRole())) {
                runAdminMenu();
            } else {
                runUserMenu(loginUser);
            }
        } else {
            System.out.println("[로그인 실패] 아이디 또는 비밀번호가 틀렸습니다.");
        }
    }

    // 회원가입 처리 메서드
    private static void handleRegister() {
        System.out.print("사용할 아이디: ");
        String newId = scanner.nextLine();
        
        if (userDAO.checkUsernameExist(newId)) {
            System.out.println("[회원가입 실패] 이미 사용 중인 아이디입니다. 다른 아이디를 입력해주세요.");
            return; 
        }
        
        System.out.print("비밀번호: ");
        String newPw = scanner.nextLine();
        System.out.print("전화번호 (예: 010-1234-5678): ");
        String newPhone = scanner.nextLine();
        
        // 기본 가입은 USER로 설정
        UserDTO newUser = new UserDTO(newId, newPw, newPhone, "USER");
        boolean success = userDAO.registerUser(newUser);

        if (success) {
            System.out.println("[회원가입 성공] 축하합니다! 로그인을 진행해주세요.");
        } else {
            System.out.println("[회원가입 실패] 입력 정보를 확인해주세요.");
        }
    }
    
    

    // 관리자 전용 메뉴 메서드
    private static void runAdminMenu() {
        boolean adminRunning = true;
        while (adminRunning) {
            System.out.println("\n--- [ 관리자 전용 메뉴 ] ---");
            System.out.println("[1] 요금 변경");
            System.out.println("[2] 전체 예약 현황 조회");
            System.out.println("[3] 로그아웃");
            System.out.print("메뉴 선택: ");
            
            String sel = scanner.nextLine();
            switch (sel) {
                case "1":
                	handleChangePrice();
                    break;
                case "2":
                    handleAllReservations();
                    break;
                case "3":
                    System.out.println("\n로그아웃합니다.");
                    adminRunning = false;
                    break;
                default:
                    System.out.println("\n잘못된 입력입니다.");
            }
        }
    }
    
    // 관리자용 시간당 요금 변경 메서드 
    private static void handleChangePrice() {
        int currentPrice = settingDAO.getPricePerHour(); // DB에서 현재 요금 조회
        
        System.out.println("\n--- [ 시간당 요금 변경 ] ---");
        System.out.println("현재 시간당 요금: " + currentPrice + "원");
        System.out.print("변경할 새로운 시간당 요금 입력 (원): ");
        
        String input = scanner.nextLine().trim();
        try {
            int newPrice = Integer.parseInt(input);
            if (newPrice <= 0) {
                System.out.println("[오류] 요금은 0원보다 커야 합니다.");
                return;
            }
            
            boolean success = settingDAO.updatePricePerHour(newPrice);
            if (success) {
                System.out.println("[성공] 시간당 요금이 " + newPrice + "원으로 변경되었습니다.");
            } else {
                System.out.println("[실패] 요금 변경 중 문제가 발생했습니다.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("[오류] 올바른 숫자를 입력해주세요.");
        }
    }
    
    // 관리자용 전체 예약 현황 조회 메서드
    private static void handleAllReservations() {
        System.out.println("\n--- [ 전체 예약 현황 조회 (관리자용) ] ---");
        
        ReservationDAO reservationDAO = new ReservationDAO();
        java.util.List<ReservationDTO> allReservations = reservationDAO.getAllReservations();
        
        if (allReservations.isEmpty()) {
            System.out.println("[안내] 등록된 예약 내역이 존재하지 않습니다.");
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // 전체 예약 목록 표 형태 출력 
        System.out.println("--------------------------------------------------------------------------------------");
        System.out.printf("%-6s | %-12s | %-12s | %-10s | %-8s | %-10s | %-10s\n", "예약번호", "예약자", "이용날짜", "이용시간", "결제금액", "결제수단", "상태");
        System.out.println("--------------------------------------------------------------------------------------");
        
        for (ReservationDTO r : allReservations) {
            String displayStatus = r.getStatus();
            
            // 시간이 지났으면 'COMPLETED'로 표시 전환
            if ("RESERVED".equals(displayStatus)) {
                try {
                    LocalDate resDate = LocalDate.parse(r.getReservationDate(), formatter);
                    LocalDateTime endDateTime = resDate.atTime(r.getEndHour(), 0);
                    
                    if (now.isAfter(endDateTime)) {
                        displayStatus = "COMPLETED";
                    }
                } catch (Exception e) {
                    // 예외 발생 시 원본 유지
                }
            }
            
            // 한글 상태명 변환
            String statusKr = displayStatus;
            if ("RESERVED".equals(displayStatus)) 
                statusKr = "예약중";
            else if ("CANCELED".equals(displayStatus)) 
                statusKr = "취소됨";
            else if ("COMPLETED".equals(displayStatus)) 
                statusKr = "이용완료";
            
            System.out.printf("%-8d | %-13s | %-14s | %2d시 ~ %4d시 | %-10d | %-12s | %-10s\n",
                r.getReservationId(),
                r.getUsername(),
                r.getReservationDate(),
                r.getStartHour(),
                r.getEndHour(),
                r.getAmount(),
                r.getPaymentMethod(),
                statusKr
            );
        }
        System.out.println("--------------------------------------------------------------------------------------");
        
        System.out.print("이전 메뉴로 돌아가려면 엔터를 누르세요.");
        scanner.nextLine();
    }

    // 일반 사용자 메뉴 메서드
    private static void runUserMenu(UserDTO user) {
        boolean userRunning = true;
        while (userRunning) {
            System.out.println("\n--- [ 사용자 메뉴 (" + user.getUsername() + "님) ] ---");
            System.out.println("[1] 공유 오피스 공간 조회 및 예약");
            System.out.println("[2] 내 예약 확인 및 취소");
            System.out.println("[3] 로그아웃");
            System.out.print("메뉴 선택: ");
            
            String sel = scanner.nextLine();
            switch (sel) {
                case "1":
                    handleReservation(user);
                    break;
                case "2":
                    handleMyReservations(user);
                    break;
                case "3":
                    System.out.println("로그아웃합니다.");
                    userRunning = false;
                    break;
                default:
                    System.out.println("잘못된 입력입니다.");
            }
        }
    }
    
    // 오피스 예약
    private static void handleReservation(UserDTO loginUser) {
        System.out.println("\n--- [ 공유 오피스 공간 예약 및 시간표 조회 ] ---");
        
        // 날짜 입력 및 형식 검사 루프
        String reservationDate = "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        while (true) {
            System.out.print("조회할 날짜 입력 (형식: YYYY-MM-DD, 예: 2026-11-15): ");
            String input = scanner.nextLine().trim();
            
            try {
                // 입력받은 문자열을 yyyy-MM-dd 형태로 파싱 
                LocalDate parsedDate = LocalDate.parse(input, formatter);
                
                // 오늘 이전 날짜는 예약 못 하게 막기
                if (parsedDate.isBefore(LocalDate.now())) {
                    System.out.println("[오류] 지난 날짜는 예약할 수 없습니다. 다시 입력해주세요.");
                    continue;
                }
                
                reservationDate = parsedDate.toString(); 
                break;
                
            } catch (DateTimeParseException e) {
                System.out.println("[오류] 날짜 형식이 올바르지 않거나 존재하지 않는 날짜입니다. (YYYY-MM-DD 형식으로 입력)");
            }
        }
        
        // 0~23시 시간표 상태 배열 생성 (기본값 'O'로 초기화)
        char[] timeSlots = new char[24];
        for (int i = 0; i < 24; i++) {
            timeSlots[i] = 'O'; 
        }
        
        ReservationDAO reservationDAO = new ReservationDAO();
        java.util.Map<Integer, Boolean> reservedMap = reservationDAO.getReservedHoursByDate(reservationDate);
        
        for (int i = 0; i < 24; i++) {
            if (reservedMap.getOrDefault(i, false)) {
                timeSlots[i] = 'X';
            }
        }
        
        // 오늘 날짜인 경우, 현재 시각 이전의 시간은 무조건 'X'
        LocalDate parsedReservationDate = LocalDate.parse(reservationDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (parsedReservationDate.isEqual(LocalDate.now())) {
            int currentHour = java.time.LocalTime.now().getHour(); // 현재 시각 (0~23)
            for (int i = 0; i <= currentHour; i++) {
                timeSlots[i] = 'X'; // 현재 시간까지는 무조건 예약 불가(X)로 표시
            }
        }
        
        // 윗줄 시간 / 아랫줄 O/X 시각화 출력
        System.out.println("\n================ [ " + reservationDate + " 예약 현황 ] ================");
        
        // 시간 출력 
        System.out.print(" 시간 |");
        for (int i = 0; i < 24; i++) {
            System.out.printf(" %2d ", i);
        }
        System.out.println();
        
        System.out.println("-------" + "----".repeat(24)); // 구분선
        
        // O/X 상태 출력
        System.out.print(" 상태 |");
        for (int i = 0; i < 24; i++) {
            System.out.print(" [" + timeSlots[i] + "]");
        }
        System.out.println("\n==================================================================");
        
        // 이용할 시간 선택
        System.out.print("이용할 시작 시간 입력 (0~23): ");
        int startHour = Integer.parseInt(scanner.nextLine());
        
        System.out.print("이용할 종료 시간 입력 (0~23): ");
        int endHour = Integer.parseInt(scanner.nextLine());
        
        // 유효성 및 중복 검사
        if (startHour >= endHour || startHour < 0 || endHour > 24) {
            System.out.println("[오류] 시간 설정이 잘못되었습니다. (시작 시간이 종료 시간보다 빠를 수 없습니다.)");
            return;
        }
        
        
        
        boolean isConflict = false;
        for (int i = startHour; i < endHour; i++) {
            if (timeSlots[i] == 'X') {
                isConflict = true;
                break;
            }
        }
        
        if (isConflict) {
            System.out.println("[예약 실패] 선택하신 시간대에 이미 예약된(X) 시간이 포함되어 있습니다.");
            return;
        }
        
        // 이용 시간 계산
        int hours = endHour - startHour;
        
        // 요금 계산
        int currentPrice = settingDAO.getPricePerHour();
        int totalAmount = hours * currentPrice;
        
        // 결제 정보 확인 및 출력
        System.out.println("\n================ [ 결제 정보 확인 ] ================");
        System.out.println("- 이용 날짜   : " + reservationDate);
        System.out.println("- 이용 시간   : " + startHour + "시 ~ " + endHour + "시 (" + hours + "시간)");
        System.out.println("- 총 결제 금액: " + totalAmount + "원");
        System.out.println("=====================================================");
        
        // 결제 수단 선택 받기
        System.out.print("결제 수단을 선택하세요 [1] 카드 결제 [2] 현금 결제: ");
        String payMethodChoice = scanner.nextLine();
        
        String paymentMethod = "";
        if ("1".equals(payMethodChoice)) {
            paymentMethod = "CARD";
        } else if ("2".equals(payMethodChoice)) {
            paymentMethod = "CASH";
        } else {
            System.out.println("[오류] 올바른 결제 수단을 선택해주세요. 예약을 취소합니다.");
            return;
        }
        
        // 최종 결제 확인
        System.out.print("위 내용으로 결제를 진행하시겠습니까? [1] 결제 확정 [2] 취소: ");
        String confirmChoice = scanner.nextLine();
        
        if (!"1".equals(confirmChoice)) {
            System.out.println("[취소] 예약 및 결제가 취소되었습니다.");
            return;
        }

        // DAO 트랜잭션 호출
        boolean isSuccess = reservationDAO.insertReservationAndPayment(
            loginUser.getUserId(), reservationDate, startHour, endHour, totalAmount, paymentMethod
        );
        
        if (isSuccess) {
            System.out.println("\n[성공] 예약 및 결제가 정상적으로 완료되었습니다!");
        } else {
            System.out.println("\n[오류] 예약 처리 중 문제가 발생했습니다. 다시 시도해주세요.");
        }
    }
    
    // 내 예약 확인 및 취소 메서드
    private static void handleMyReservations(UserDTO loginUser) {
        System.out.println("\n--- [ 내 예약 확인 및 취소 ] ---");
        
        ReservationDAO reservationDAO = new ReservationDAO();
        java.util.List<ReservationDTO> myReservations = reservationDAO.getMyReservations(loginUser.getUserId());
        
        if (myReservations.isEmpty()) {
            System.out.println("[안내] 예약 내역이 존재하지 않습니다.");
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // 예약 목록 표 형태 출력
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-6s | %-12s | %-10s | %-8s | %-10s | %-8s\n", "예약번호", "이용날짜", "이용시간", "결제금액", "결제수단", "상태");
        System.out.println("--------------------------------------------------------------------------");
        
        for (ReservationDTO r : myReservations) {
            String displayStatus = r.getStatus();
            
            // 상태가 'RESERVED'인데 이용 종료 시간이 현재 시간보다 지났다면 'COMPLETED'로 전환
            if ("RESERVED".equals(displayStatus)) {
                try {
                    LocalDate resDate = LocalDate.parse(r.getReservationDate(), formatter);
                    LocalDateTime endDateTime = resDate.atTime(r.getEndHour(), 0);
                    
                    if (now.isAfter(endDateTime)) {
                        displayStatus = "COMPLETED";
                    }
                } catch (Exception e) {
                    // 예외 발생 시 원본 상태 유지
                }
            }
            
            // 한글 상태명 변환
            String statusKr = displayStatus;
            if ("RESERVED".equals(displayStatus)) 
                statusKr = "예약중";
            else if ("CANCELED".equals(displayStatus)) 
                statusKr = "취소됨";
            else if ("COMPLETED".equals(displayStatus)) 
                statusKr = "이용완료";
            
            System.out.printf("%-8d | %-14s | %2d시 ~ %4d시 | %-10d | %-12s | %-8s\n",
                r.getReservationId(),
                r.getReservationDate(),
                r.getStartHour(),
                r.getEndHour(),
                r.getAmount(),
                r.getPaymentMethod(),
                statusKr
            );
        }
        System.out.println("--------------------------------------------------------------------------");
        
        System.out.print("취소할 예약 번호를 입력하세요 (돌아가려면 0 입력): ");
        String inputStr = scanner.nextLine().trim();
        
        if (inputStr.isEmpty() || "0".equals(inputStr)) {
            return;
        }
        
        int cancelReservationId;
        try {
            cancelReservationId = Integer.parseInt(inputStr);
        } catch (NumberFormatException e) {
            System.out.println("[오류] 올바른 숫자를 입력해주세요.");
            return;
        }
        
        // DAO를 통해 취소 처리 실행 
        boolean isCanceled = reservationDAO.cancelReservation(cancelReservationId, loginUser.getUserId());
        
        if (isCanceled) {
            System.out.println("[성공] 예약이 정상적으로 취소되었습니다. 해당 시간대가 다시 예약 가능 상태로 변경됩니다.");
        } else {
            System.out.println("[실패] 예약 취소에 실패했습니다. 본인의 예약 번호가 맞는지 확인해주세요.");
        }
        
        // 메인 화면으로 돌아가기
        System.out.print("이전 메뉴로 돌아가려면 엔터를 누르세요.");
        scanner.nextLine();
    }
}