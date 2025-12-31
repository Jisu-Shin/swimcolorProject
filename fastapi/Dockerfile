# 1. Python 베이스 이미지
FROM python:3.9-slim

# 2. 필수 패키지 및 Chrome 종속성 설치
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    unzip \
    curl \
    xvfb \
    libxi6 \
    default-jdk \
    libnss3 \
    libatk-bridge2.0-0 \
    libgtk-3-0 \
    && apt-get clean

# 3. Google Chrome 설치 (apt-key 대신 gpg 키 저장소 방식 사용)
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/googlechrome-linux-keyring.gpg \
    && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/googlechrome-linux-keyring.gpg] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list \
    && apt-get update && apt-get install -y google-chrome-stable

# 4. 작업 디렉토리 설정
WORKDIR /app

# 5. 의존성 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 6. 소스 코드 복사
COPY . .

# 7. YOLO가 설정 파일을 쓸 수 있는 임시 폴더
ENV YOLO_CONFIG_DIR=/tmp

# 8. 실행
EXPOSE 8000
CMD ["python", "run.py"]