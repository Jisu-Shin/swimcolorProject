# ============================================
# 실행 전용 Dockerfile (GitHub Actions에서 빌드 완료)
# ============================================
FROM amazoncorretto:17-alpine

# 작업 디렉토리 설정
WORKDIR /app

# GitHub Actions에서 빌드된 JAR 파일 복사
COPY spring-boot/build/libs/*-SNAPSHOT.jar app.jar

# 타임존 설정
ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

# 애플리케이션 포트 노출
EXPOSE 8080

# JVM 옵션 설정 (메모리 최적화)
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseContainerSupport"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Dspring.profiles.active=prod -jar /app/app.jar"]