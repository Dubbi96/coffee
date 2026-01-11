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
RUN printf '#!/bin/sh\necho "Starting entrypoint script..."\nif [ -n "$GCP_SA_KEY_B64" ]; then\n  echo "Decoding GCP service account key..."\n  echo "$GCP_SA_KEY_B64" | base64 -d > /app/coffee-backend-key.json 2>/dev/null\n  EXIT_CODE=$?\n  if [ $EXIT_CODE -eq 0 ]; then\n    chmod 600 /app/coffee-backend-key.json\n    echo "Service account key file created successfully"\n  else\n    echo "ERROR: Failed to decode GCP service account key" >&2\n    exit 1\n  fi\nelse\n  echo "ERROR: GCP_SA_KEY_B64 environment variable is not set" >&2\n  exit 1\nfi\necho "Starting Java application..."\nexec java -jar /app/Coffee-1.0.0.jar\n' > /app/entrypoint.sh && chmod +x /app/entrypoint.sh

CMD ["/app/entrypoint.sh"]