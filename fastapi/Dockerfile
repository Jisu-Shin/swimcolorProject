# 1. Python 베이스 이미지 (가벼운 slim)
FROM python:3.9-slim

# 2. # 시스템 의존성 설치
RUN apt-get update && apt-get install -y \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
# 3. 작업 디렉토리
WORKDIR /app

# 4. 의존성 파일 복사
COPY requirements-dev.txt .

# 6. 기타 라이브러리 설치
RUN pip install --upgrade pip \
 && pip install --no-cache-dir -r requirements-dev.txt
RUN playwright install --with-deps chromium

# 7. 소스 코드 복사
COPY . .

# 8. 환경 변수
ENV YOLO_CONFIG_DIR=/tmp
ENV CHROME_PATH=/usr/bin/google-chrome
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# 9. 포트
EXPOSE 8000

# 10. 🔥 FastAPI 실행 (Gunicorn + 600초 타임아웃)
CMD ["sh", "-c", "\
gunicorn run:app \
-k uvicorn.workers.UvicornWorker \
--workers ${GUNICORN_WORKERS:-2} \
--bind 0.0.0.0:8000 \
--timeout ${GUNICORN_TIMEOUT:-600} \
--access-logfile - \
--error-logfile - \
--capture-output \
--log-level ${LOG_LEVEL:-info} \
"]