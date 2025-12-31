# 1. Python 베이스 이미지 (가벼운 slim 버전)
FROM python:3.9-slim

# 2. 필수 패키지 및 Chrome 설치 (레이어 합치기 + 캐시 삭제로 용량 최적화)
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    unzip \
    curl \
    xvfb \
    libxi6 \
    libnss3 \
    libatk-bridge2.0-0 \
    libgtk-3-0 \
    && wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/googlechrome-linux-keyring.gpg \
    && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/googlechrome-linux-keyring.gpg] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list \
    && apt-get update && apt-get install -y google-chrome-stable \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# 3. 작업 디렉토리 설정
WORKDIR /app

# 4. 의존성 파일 복사
COPY requirements.txt .

# 5. [핵심] PyTorch CPU 버전 설치 (7GB -> 수백 MB로 줄어드는 구간)
# --no-cache-dir 옵션으로 빌드 시 임시 파일도 남기지 않습니다.
RUN pip install --no-cache-dir torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cpu

# 6. 기타 라이브러리 설치
RUN pip install --no-cache-dir -r requirements.txt

# 7. 소스 코드 및 모델 파일(.pt) 복사
# .dockerignore 덕분에 venv는 제외되고 모델과 코드만 쏙 들어갑니다.
COPY . .

# 8. YOLO 환경 설정 및 포트 개방
ENV YOLO_CONFIG_DIR=/tmp
EXPOSE 8000

# 9. 실행 (run.py 내부에서 포트 설정을 확인하세요)
CMD ["python", "run.py"]