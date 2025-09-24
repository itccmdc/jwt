# JWT 채용 과제 - 로그인 시스템

## 기술 스택
- **Language**: Java 17  
- **Framework**: Spring Boot 3.5.6  
- **Database**: H2 (in-memory)  
- **JDBC URL**: `jdbc:h2:mem:testdb`

---

## 실행 방법

### ▶ 방법 1: Jar 실행
1. **단축키** Ctrl + \` (VSCode 기준) → 터미널 실행  
2. `.\gradlew clean build` 입력 후 **Enter**  
3. `java -jar build/libs/jwt-0.0.1-SNAPSHOT.jar` 입력 후 **Enter**  
4. 브라우저 실행 → [Swagger UI](http://localhost:8080/swagger-ui/index.html) 접속  

### ▶ 방법 2: BootRun 실행
1. **단축키** Ctrl + \` (VSCode 기준) → 터미널 실행  
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

## DataBase (H2 선택 이유)
- **단점**: 운영 환경에서는 **데이터 영속성**, **대규모 처리 성능** 등의 문제로 인해 별도 DBMS로의 전환이 필요합니다.  
- **장점**:  
  - **개발 및 테스트 편의성**이 뛰어납니다.  
  - H2는 인메모리 기반이라 설치가 필요 없고, 애플리케이션 실행과 함께 **DB 환경을 즉시 구성**할 수 있어 개발 속도가 빠릅니다.  

이러한 이유로 본 과제에서는 **H2**를 선택하였습니다.  

---

## 환경 설정 (JWT)
```yaml
jwt:
  access:
    secret: f8a7c2e1b5d943f28a1c6e4d9f0b3a7c
    ttl: 900000        # 15 minutes (ms)
  refresh:
    secret: 3d9f7b2c1e5a48f6b0d2c9e7f4a1b6c8
    ttl: 1209600000    # 14 days (ms)
```

# ===============================
# JWT 인증 설계 문서
# ===============================

## 1. 개요
- JWT(JSON Web Token) 기반 인증/인가 설계 정의  
- Access Token과 Refresh Token을 활용한 인증 및 세션 관리 정책 설명  

## 2. 토큰 종류 및 만료 정책
- **Access Token**  
  · 유효기간: 15분  
  · 용도: API 호출 시 사용자 인증 및 권한 확인  

- **Refresh Token**  
  · 유효기간: 14일  
  · 용도: Access Token 갱신  

- **설정 근거**  
  · 보안성(짧을수록 안전)과 사용자 편의성(길수록 로그인 유지 용이)의 균형 고려  

## 3. JWT Claim 정의
- `id` : 사용자 고유 ID  
- `email` : 사용자 이메일  
- `role` : 사용자 권한 (예: USER, ADMIN)  
- `iat` : 토큰 발급 시간 (Issued At)  
- `exp` : 토큰 만료 시간 (Expiration)  

## 4. 발급 및 사용 흐름
1) **로그인 요청** (`/auth/login`) → Access Token + Refresh Token 발급  
2) **API 요청** → 클라이언트는 Authorization 헤더에 Access Token 포함  
3) **Access Token 만료 시** (`/auth/refresh`) → Refresh Token 제출 → 새로운 Access Token 발급  
4) **로그아웃** → Refresh Token 폐기  

## 5. 보안 정책
- **통신 보안**: HTTPS 필수  
- **토큰 저장 위치**: HTTPOnly Cookie 권장  
- **서명 알고리즘 선택**: **HS256**  
  - 장점: RS256보다 빠르고 키 관리가 단순함  
  - 단점: 대칭키 유출 시 위험 → 안전한 키 관리 필요  
  - 본 과제는 단일 서버 환경이므로 **HS256**을 선택  

- **탈취 대응 방안**  
  · Access Token 짧은 만료 시간 유지  
  · Refresh Token 재발급 시 기존 토큰 무효화  
  · 사용자 단말/UA/IP 등 검증 추가 가능  

## 6. 예외 처리
- 토큰 만료 → **401 Unauthorized**  
- 서명 불일치 → **401 Unauthorized**  
- 권한 부족 → **403 Forbidden**  

## 7. JWT 예시
```json
{
  "sub": "user123",
  "email": "user@example.com",
  "role": "ADMIN",
  "iat": 1695538200,
  "exp": 1695539100
}
```