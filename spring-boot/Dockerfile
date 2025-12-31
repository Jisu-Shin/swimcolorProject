# 1. Java 17 버전 베이스 이미지 (프로젝트 버전에 맞춰 수정 가능)
FROM amazoncorretto:17-alpine

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드된 JAR 파일을 이미지 내부로 복사
# Gradle은 보통 build/libs/ 아래에 -plain.jar와 일반 jar 두 개를 만드는데,
# 실행 가능한 jar만 가져오기 위해 아래처럼 경로를 지정하는 게 안전해.
ARG JAR_FILE=build/libs/*-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 4. 실행 환경 변수 설정 (필요시)
ENV TZ=Asia/Seoul

# 5. 포트 설정 (Spring Boot 기본값은 8080)
EXPOSE 8080

# 6. JAR 실행
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]