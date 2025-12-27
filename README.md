# 🏊 SwimColor Project

색상 기반 수영복/수모 매칭 추천 서비스

## 📋 개요

SwimColor는 이미지 색상 분석을 통해 수영복과 수모의 최적 조합을 추천하는 AI 기반 서비스입니다. 
사용자가 업로드한 수영복 이미지를 분석하여 색상 조화를 고려한 수모를 추천합니다.

## 🏗️ 프로젝트 구조

```
swimcolorProject/
├── fastapi/              # Python 백엔드 (AI/ML 서비스)
│   ├── app/             # FastAPI 애플리케이션
│   ├── ml/              # 머신러닝 모델 및 색상 분석
│   ├── test/            # 테스트 코드
│   ├── requirements.txt # Python 의존성
│   └── run.py           # 실행 진입점
│
├── spring-boot/         # Java 백엔드 (비즈니스 로직)
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   ├── build.gradle     # Gradle 빌드 설정
│   └── gradlew          # Gradle 래퍼
│
└── README.md            # 프로젝트 문서 (이 파일)
```

## 🎨 주요 기능

### 색상 추출 & 분석
- **KMeans 클러스터링**: 이미지에서 지배색 및 색상 팔레트 추출 (n_colors=3)
- **CIE Lab 색공간**: 인간의 색각과 유사한 색상 거리 계산
- **다양한 추천 알고리즘**:
  - 유사색 (Analogous) 추천
  - 보색 (Complementary) 추천
  - 삼원색 (Triadic) 추천

### 이미지 처리
- Pillow, OpenCV를 활용한 이미지 전처리
- 배경 제거를 통한 정확한 색상 추출 (옵션)
- 다양한 이미지 포맷 지원

## 🚀 시작하기

### 필수 요구사항
- Python 3.9+
- Java 17+
- Gradle 7.0+

### FastAPI 서버 실행

```bash
cd fastapi

# 가상환경 생성 및 활성화
python -m venv .venv
source .venv/bin/activate  # Mac/Linux
# .venv\Scripts\activate   # Windows

# 의존성 설치
pip install -r requirements.txt

# 서버 실행
python run.py
```

FastAPI 서버는 기본적으로 `http://localhost:8000`에서 실행됩니다.
- API 문서: `http://localhost:8000/docs`
- ReDoc: `http://localhost:8000/redoc`

### Spring Boot 서버 실행

```bash
cd spring-boot

# Gradle로 빌드 및 실행
./gradlew bootRun

# 또는 빌드 후 실행
./gradlew build
java -jar build/libs/*.jar
```

## 📡 API 엔드포인트

### FastAPI (AI/ML 서비스)
- `POST /api/analyze` - 이미지 색상 분석
- `POST /api/recommend` - 색상 기반 추천
- `GET /api/health` - 헬스 체크

### Spring Boot (비즈니스 로직)
- `GET /api/swimsuits` - 수영복 목록 조회
- `GET /api/swimsuits/{id}` - 특정 수영복 조회
- `GET /api/swimcaps` - 수모 목록 조회
- `POST /api/swimsuits/{id}/recommend` - 수모 추천

## 🧪 테스트

### FastAPI 테스트
```bash
cd fastapi
pytest
# 또는 커버리지 포함
pytest --cov=app --cov-report=html
```

### Spring Boot 테스트
```bash
cd spring-boot
./gradlew test
```

## 🛠️ 기술 스택

### Backend (FastAPI)
- **FastAPI**: 고성능 Python 웹 프레임워크
- **OpenCV**: 이미지 처리
- **scikit-learn**: KMeans 클러스터링
- **scikit-image**: 색공간 변환 (RGB ↔ Lab)
- **Pillow**: 이미지 로딩 및 조작
- **NumPy**: 수치 연산

### Backend (Spring Boot)
- **Spring Boot 3.x**: Java 엔터프라이즈 프레임워크
- **Spring Data JPA**: 데이터 영속성
- **Gradle**: 빌드 도구

## 🎯 향후 개발 계획

- [ ] 프론트엔드 (React/Vue) 연동
- [ ] 사용자 업로드 이미지 처리 파이프라인
- [ ] 추천 알고리즘 비교 및 평가 시스템
- [ ] 패턴 기반 추천 (스트라이프, 플라워 등)
- [ ] 배경 제거를 통한 색상 추출 정확도 개선
- [ ] 대용량 이미지 처리를 위한 비동기 큐 시스템
- [ ] 사용자 피드백 기반 추천 개선

## 👥 개발 환경

### IntelliJ IDEA 설정
1. Project SDK: Java 17 선택
2. Python Interpreter: fastapi/.venv 선택
3. Gradle: 자동 가져오기 활성화

### 디버깅
- FastAPI: `uvicorn app.main:app --reload --host 0.0.0.0 --port 8000`
- Spring Boot: IntelliJ의 Run/Debug Configuration 사용

## 📄 라이선스

이 프로젝트는 개인 프로젝트입니다.

## 📧 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해주세요.