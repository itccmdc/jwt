# JWT 채용 과제 - 로그인 시스템

## 기술 스택
- **Language**: Java 17  
- **Framework**: Spring Boot 3.5.6  
- **Database**: H2 (in-memory)  
- **JDBC URL**: `jdbc:h2:mem:testdb`

---

## 실행 방법

### ▶ 방법 1: Jar 실행
1. **단축키** `Ctrl + \`` (VSCode 기준) → 터미널 실행  
2. `.\gradlew clean build` 입력 후 **Enter**  
3. `java -jar build/libs/jwt-0.0.1-SNAPSHOT.jar` 입력 후 **Enter**  
4. 브라우저 실행 → [Swagger UI](http://localhost:8080/swagger-ui/index.html) 접속  

### ▶ 방법 2: BootRun 실행
1. **단축키** `Ctrl + \`` (VSCode 기준) → 터미널 실행  
2. `.\gradlew bootRun` 입력 후 **Enter**  
3. 브라우저 실행 → [Swagger UI](http://localhost:8080/swagger-ui/index.html) 접속  

---

## H2 Database 접속
1. 브라우저 실행 → [H2 Console](http://localhost:8080/h2-console) 접속  
2. JDBC URL 입력: `jdbc:h2:mem:testdb`  
3. **Connect** 클릭 후 접속  

---

## 시스템 기능 사용 방법

1. **테이블 확인**  
   - H2 콘솔 → `show tables` 입력 후 실행  

2. **회원가입**  
   - Swagger UI → `/auth/register`  
   - `"Try it out"` 클릭 → `email`, `password`, `name` 입력 → 실행  

3. **로그인**  
   - Swagger UI → `/auth/login`  
   - `"Try it out"` 클릭 → `email`, `password` 입력 → 실행  
   - Response Body → `accessToken` 복사  
   - Swagger 상단 `"Authorize"` 버튼 클릭 → 복사한 토큰 입력 → Authorize  

4. **내 정보 확인**  
   - Swagger UI → `/users/me`  
   - `"Try it out"` → 실행 → Server response 확인  

5. **토큰 재발급 (Refresh)**  
   - Swagger UI → `/auth/refresh`  
   - `"Try it out"` → `refreshToken` 입력 → 실행 → Server response 확인  

6. **로그아웃**  
   - Swagger UI → `/auth/logout`  
   - 인증된 상태에서 실행 시 토큰 무효화 처리  

---

## JWT 서명 알고리즘 (HS256 선택 이유)

- **HS256 (대칭키 알고리즘)**  
  - 단점: 키가 유출되면 위험 (보안 취약)  
  - 장점:  
    - RS256보다 **연산이 가볍고 성능이 뛰어남**  
    - **키 하나만 관리**하면 되어 단순함  
    - **단일 서버 또는 내부 전용 서비스 구조**에서는 충분히 안전  

👉 이러한 이유로 본 과제에서는 **HS256**을 적용하였습니다.  

---