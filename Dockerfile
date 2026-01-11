# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS stage1
WORKDIR /opt/app
COPY pom.xml .
COPY ./src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jdk
WORKDIR /app

# coffee-backend-key.json은 이미지에 포함하지 않음
# Cloud Run에서 환경 변수나 Secret Manager를 통해 주입
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/coffee-backend-key.json

COPY --from=stage1 /opt/app/target/Coffee-1.0.0.jar /app/Coffee-1.0.0.jar

EXPOSE 8080

ENV PORT=8080

# 시작 스크립트: 환경 변수에서 base64 인코딩된 JSON을 디코딩하여 파일로 저장 (런타임에 주입)
RUN echo '#!/bin/sh' > /app/entrypoint.sh && \
    echo 'echo "Starting entrypoint script..."' >> /app/entrypoint.sh && \
    echo 'if [ -n "$$GCP_SA_KEY_B64" ]; then' >> /app/entrypoint.sh && \
    echo '  echo "Decoding GCP service account key..."' >> /app/entrypoint.sh && \
    echo '  echo "$$GCP_SA_KEY_B64" | base64 -d > /app/coffee-backend-key.json 2>/dev/null' >> /app/entrypoint.sh && \
    echo '  if [ $$? -eq 0 ]; then' >> /app/entrypoint.sh && \
    echo '    chmod 600 /app/coffee-backend-key.json' >> /app/entrypoint.sh && \
    echo '    echo "Service account key file created successfully"' >> /app/entrypoint.sh && \
    echo '  else' >> /app/entrypoint.sh && \
    echo '    echo "ERROR: Failed to decode GCP service account key" >&2' >> /app/entrypoint.sh && \
    echo '    exit 1' >> /app/entrypoint.sh && \
    echo '  fi' >> /app/entrypoint.sh && \
    echo 'else' >> /app/entrypoint.sh && \
    echo '  echo "ERROR: GCP_SA_KEY_B64 environment variable is not set" >&2' >> /app/entrypoint.sh && \
    echo '  exit 1' >> /app/entrypoint.sh && \
    echo 'fi' >> /app/entrypoint.sh && \
    echo 'echo "Starting Java application..."' >> /app/entrypoint.sh && \
    echo 'exec java -jar /app/Coffee-1.0.0.jar' >> /app/entrypoint.sh && \
    chmod +x /app/entrypoint.sh

CMD ["/app/entrypoint.sh"]