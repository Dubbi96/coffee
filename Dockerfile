# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS stage1
WORKDIR /opt/app
COPY pom.xml .
COPY ./src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jdk
WORKDIR /app

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/coffee-backend-key.json
COPY coffee-backend-key.json /app/coffee-backend-key.json

COPY --from=stage1 /opt/app/target/Coffee-1.0.0.jar /app/Coffee-1.0.0.jar

EXPOSE 8080  # ✅ Cloud Run이 감지할 수 있도록 포트 노출

# (선택) 환경변수에서 포트를 받을 경우 application.yml도 수정 필요
ENV PORT=8080

CMD ["java", "-jar", "/app/Coffee-1.0.0.jar"]