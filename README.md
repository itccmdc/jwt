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

# 한 번에 실행
gradlew bootRun

# H2 DB URL
http://localhost:8080/h2-console