# jwt
채용 과제 - 로그인

# 스택
Spring Java (Java 17, Spring Boot 3.5.6)
DB: H2

# JDBC URL
jdbc:h2:mem:testdb

# 실행방법
gradlew clean build
java -jar build/libs/jwt-0.0.1-SNAPSHOT.jar

# 해야 할 것
1. 토큰 재발급 시 토큰 회수
2. 로그아웃 시 토큰 회수
3. users/me 확인