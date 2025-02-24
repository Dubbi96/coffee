# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS stage1
WORKDIR /opt/app
COPY pom.xml .
COPY ./src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 환경 변수 설정
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/coffee-backend-key.json
COPY coffee-backend-key.json /app/coffee-backend-key.json

# 빌드된 JAR 파일 복사
COPY --from=stage1 /opt/app/target/Coffee-1.0.0.jar /app/Coffee-1.0.0.jar

ENV PORT=8080

# Cloud Run에서 자동 할당된 포트 사용하도록 설정
CMD ["java", "-jar", "/app/Coffee-1.0.0.jar"]
