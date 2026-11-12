# 🏢 공유 오피스 관리 및 예약 시스템

Java, JDBC, Oracle Database를 활용하여 구현한 콘솔 기반의 공유 오피스 관리 및 예약 프로그램입니다.  
일반 사용자의 시간대별 공간 조회·예약·결제·취소 기능과, 관리자의 시간당 요금 영구 변경 및 전체 예약 현황 조회 기능을 제공합니다.

---

## 🛠️ 기술 스택
- **Language:** Java (JDK 17+)
- **Database:** Oracle Database 21c XE
- **Connectivity:** JDBC (ojdbc)
- **Architecture:** MVC / DAO-DTO 패턴 적용

---

## ✨ 주요 기능

### 👤 일반 사용자
- **회원가입 및 로그인:** 아이디 중복 체크 및 권한(`USER` / `ADMIN`)별 메뉴 분기
- **실시간 예약 현황 시각화:** 선택한 날짜의 0시~23시 예약 가능 여부를 `[O]` / `[X]`로 시각화 (당일 현재 시각 이전 시간대는 자동 `[X]` 마감 처리)
- **공간 예약 및 결제:** 중복 시간대 예약 방지, 시간당 요금 자동 계산, 결제 수단(`CARD` / `CASH`) 선택 및 DB 트랜잭션 처리
- **내 예약 확인 및 취소:** 본인의 예약 내역 조회, 이용 종료 시간 경과 시 자동 `이용완료` 상태 표시 및 예약 취소

### 👑 관리자
- **시간당 요금 변경:** DB(`office_settings`) 연동을 통한 시간당 이용 요금 영구 변경 및 즉시 반영
- **전체 예약 현황 조회:** 모든 사용자의 예약 내역, 결제 금액, 결제 수단, 이용 상태(`예약중` / `이용완료` / `취소됨`) 통합 조회

---

## 📂 패키지 구조
```text
src/com/officemanagement/
├── db/
│   └── DBConnection.java      # db.properties 기반 Oracle DB 연결 관리
├── dao/
│   ├── UserDAO.java           # 회원 로그인, 회원가입, 중복 확인
│   ├── ReservationDAO.java    # 예약/결제 트랜잭션 처리, 현황 조회 및 취소
│   └── SettingDAO.java        # 시간당 요금 조회 및 DB 영구 업데이트
├── model/
│   ├── UserDTO.java           # 회원 데이터 전송 객체
│   └── ReservationDTO.java    # 예약 및 결제 통합 데이터 전송 객체
└── main/
    └── MainApp.java           # 콘솔 UI 및 전체 메뉴 실행 진입점
```

---

## 🚀 실행 방법 (Getting Started)

### 1. Oracle 데이터베이스 테이블 생성
Oracle SQL Developer 등에서 아래 쿼리를 실행하여 필수 테이블과 초기 데이터를 생성합니다.

```sql
-- 1. 회원 정보 테이블
CREATE TABLE users (
    user_id NUMBER GENERATED AS IDENTITY PRIMARY KEY,
    username VARCHAR2(50) NOT NULL UNIQUE,
    password VARCHAR2(100) NOT NULL,
    phone VARCHAR2(20),
    role VARCHAR2(20) DEFAULT 'USER',
    created_at DATE DEFAULT SYSDATE
);

-- 관리자 기본 계정 등록 (ID: admin / PW: 1234)
INSERT INTO users (username, password, phone, role) 
VALUES ('admin', '1234', '010-0000-0000', 'ADMIN');

-- 2. 예약 정보 테이블
CREATE TABLE reservations (
    reservation_id NUMBER GENERATED AS IDENTITY PRIMARY KEY,
    user_id NUMBER,
    reservation_date VARCHAR2(20),
    start_hour NUMBER,
    end_hour NUMBER,
    status VARCHAR2(20) DEFAULT 'RESERVED',
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 3. 결제 정보 테이블
CREATE TABLE payments (
    payment_id NUMBER GENERATED AS IDENTITY PRIMARY KEY,
    reservation_id NUMBER,
    amount NUMBER,
    payment_method VARCHAR2(30),
    payment_date DATE DEFAULT SYSDATE,
    CONSTRAINT fk_payment_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
);

-- 4. 오피스 설정 테이블 (요금 관리)
CREATE TABLE office_settings (
    setting_key VARCHAR2(50) PRIMARY KEY,
    setting_value VARCHAR2(100) NOT NULL
);

-- 기본 시간당 요금 데이터 입력 (5,000원)
INSERT INTO office_settings (setting_key, setting_value) 
VALUES ('PRICE_PER_HOUR', '5000');

COMMIT;
```

### 2. DB 설정 파일(`db.properties`) 생성
보안을 위해 실제 DB 접속 정보는 `.gitignore`로 제외되어 있습니다. 프로젝트 최상단 루트 디렉토리에 `db.properties` 파일을 생성한 뒤 본인 환경에 맞게 작성해 주세요.

```properties
db.driver=oracle.jdbc.driver.OracleDriver
db.url=jdbc:oracle:thin:@localhost:1521:XE
db.username=YOUR_ORACLE_ID
db.password=YOUR_ORACLE_PASSWORD
```

### 3. 프로그램 실행
`com.officemanagement.main.MainApp` 클래스를 실행합니다.
