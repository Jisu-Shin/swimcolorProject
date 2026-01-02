# 🏊 SwimColor

AI 기반 수영복/수모 색상 매칭 추천 서비스

> **"감이 아닌 데이터로, 당신의 수영 스타일을 완성하세요"**

[![Demo](https://img.shields.io/badge/Demo-Live-success)](https://bit.ly/swimcolor-project)
[![AWS](https://img.shields.io/badge/AWS-App%20Runner-orange)](https://aws.amazon.com/)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.127.0-009688)](https://fastapi.tiangolo.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F)](https://spring.io/)

## 📋 서비스 개요

### 기획 의도 및 배경

**실제 경험 기반의 문제 인식**
- 수영복 구매 시 매번 반복되는 '어울리는 수모 찾기'의 번거로움과 시간 낭비
- 온라인 쇼핑에서 실제 착용 시 색상 조합을 미리 확인하기 어려운 문제
- 감각에만 의존하는 코디가 아닌, 객관적인 색상 조합 가이드의 필요성

**타겟 사용자**
- 20대~50대 여성 수영인
- 자신만의 스타일을 중시하고 색상 조합에 관심이 많은 사용자
- 합리적인 소비를 추구하며 구매 전 충분한 정보를 원하는 사용자

**제작 목적**
- 데이터와 AI 알고리즘에 기반한 객관적인 수영 스타일 가이드 제공
- 수영용품 구매 시 의사결정 시간 단축 및 만족도 향상
- 색상 이론(CIEDE2000)을 활용한 과학적인 추천 시스템 구현

## 🏗️ 시스템 아키텍처

### 전체 구조 개요

```
┌─────────────────────────────────────────────────────────────────┐
│                          사용자                                  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot (Port 8080)                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Controller (REST API / Web MVC)                          │  │
│  └────────────────────────┬─────────────────────────────────┘  │
│                           │                                      │
│  ┌────────────────────────▼─────────────────────────────────┐  │
│  │  Service Layer (비즈니스 로직)                            │  │
│  │  - 수영복/수모 관리                                        │  │
│  │  - FastAPI 호출 (추천 요청)                               │  │
│  └────────────────────────┬─────────────────────────────────┘  │
│                           │                                      │
│  ┌────────────────────────▼─────────────────────────────────┐  │
│  │  Repository (JPA)                                         │  │
│  └────────────────────────┬─────────────────────────────────┘  │
└───────────────────────────┼──────────────────────────────────────┘
                            │
                            ▼
                  ┌──────────────────┐
                  │  MySQL Database  │
                  │  - Swimsuit      │
                  │  - Swimcap       │
                  │  - Palette       │
                  └──────────────────┘

                            │ HTTP Request
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                     FastAPI (Port 8000)                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  REST API Endpoints                                       │  │
│  │  - /crawl/swimsuits    (크롤링 + 색상 추출)              │  │
│  │  - /crawl/swimcaps     (크롤링 + 색상 추출)              │  │
│  │  - /recommend          (색상 유사도 계산)                 │  │
│  └────────────────────────┬─────────────────────────────────┘  │
│                           │                                      │
│  ┌────────────────────────▼─────────────────────────────────┐  │
│  │  AI/ML Services                                           │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐  │  │
│  │  │ Crawler      │  │ Color        │  │ Similarity    │  │  │
│  │  │ Service      │  │ Extractor    │  │ Service       │  │  │
│  │  │              │  │              │  │               │  │  │
│  │  │ Selenium     │  │ YOLOv8 +     │  │ CIEDE2000     │  │  │
│  │  │ 동적 크롤링   │  │ K-means      │  │ 색상 유사도    │  │  │
│  │  └──────────────┘  └──────────────┘  └───────────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 마이크로서비스 분리 전략

**Spring Boot (비즈니스 로직 계층)**
- **역할**: 데이터 관리, 사용자 요청 처리, 비즈니스 규칙 적용
- **선택 이유**: 
  - 엔터프라이즈급 안정성과 확장성
  - JPA를 통한 효율적인 데이터베이스 관리
  - 객체지향적 코드 구조로 유지보수성 향상

**FastAPI (AI/ML 처리 계층)**
- **역할**: 이미지 크롤링, 객체 탐지, 색상 추출, 추천 알고리즘
- **선택 이유**:
  - Python 기반 AI/ML 라이브러리 생태계 활용
  - 비동기 처리로 크롤링 및 이미지 처리 성능 최적화
  - YOLOv8, OpenCV 등 컴퓨터 비전 라이브러리와의 자연스러운 통합

### 배포 환경

```
GitHub Repository
      │
      │ git push
      ▼
GitHub Actions (CI/CD)
      │
      │ Build & Push
      ▼
Amazon ECR (Container Registry)
      │
      │ Deploy
      ▼
AWS App Runner
      │
      ├─ Spring Boot Container (Auto Scaling)
      └─ FastAPI Container (Auto Scaling)
```

- **CI/CD**: GitHub Actions를 통한 자동 빌드 및 배포
- **컨테이너 레지스트리**: Amazon ECR
- **서버리스 컨테이너**: AWS App Runner (자동 스케일링)
- **프로덕션 URL**: https://bit.ly/swimcolor-project

## 🎨 핵심 기능 및 처리 흐름

### 1. 이미지 → 색상 추출 파이프라인

```
상품 URL 입력
      │
      ▼
┌─────────────────────┐
│  Selenium 크롤링    │  ← swim.co.kr 쇼핑몰에서 상품 정보 수집
│  - 상품명, 브랜드   │
│  - 가격, 이미지 URL │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ 이미지 다운로드      │  ← URL에서 이미지 다운로드
│ (Pillow + Requests) │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ YOLOv8 Segmentation │  ← 수영복/수모 객체만 정밀하게 추출
│ - 배경 제거         │     (배경 노이즈 완전 제거)
│ - 마스킹 처리       │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ 노이즈 필터링        │  ← 극단적으로 밝거나 어두운 픽셀 제거
│ (25 < 밝기 < 230)   │     (그림자, 반사광 제거)
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ K-means 클러스터링  │  ← 주요 색상 3개 추출
│ (n_clusters=3)      │     (대표 색상만 선별)
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ RGB → HEX 변환      │  ← 웹에서 사용 가능한 색상 코드로 변환
│ 예: #e3a1ca         │
└──────────┬──────────┘
           │
           ▼
      색상 데이터
   [#e3a1ca, #be77a0, #592d39]
```

### 2. 색상 기반 추천 알고리즘

```
수영복 색상 입력
[#e3a1ca, #be77a0, #592d39]
           │
           ▼
┌─────────────────────┐
│ HEX → LAB 변환      │  ← 인간 시각 특성을 반영한 색공간 변환
│ (OpenCV)            │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ 수모 색상 DB 조회    │  ← MySQL에서 모든 수모 색상 팔레트 로드
│ (전체 팔레트)       │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ CIEDE2000 거리 계산 │  ← 수영복 색상과 수모 색상 간 유사도 계산
│ (모든 조합)         │     (사람 눈으로 보는 것과 동일한 기준)
│                     │
│ 수영복 색1 vs 수모A │
│ 수영복 색1 vs 수모B │
│ 수영복 색2 vs 수모A │
│ ...                 │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ 임계값 필터링        │  ← 유사도 < 9.0 인 조합만 선택
│ (distance < 9.0)    │     (너무 다른 색상 제외)
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ 중복 제거           │  ← 동일 수모의 다른 색상 중
│ (수모 ID 기준)      │     가장 유사한 조합만 유지
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ 유사도 순 정렬      │  ← 가장 잘 어울리는 순서로 정렬
│ Top-5 추출          │
└──────────┬──────────┘
           │
           ▼
     추천 결과
┌─────────────────────┐
│ 1. 수모A (#e5a3cc)  │  similarity: 2.34
│ 2. 수모B (#d89ab5)  │  similarity: 3.87
│ 3. 수모C (#c678a2)  │  similarity: 5.21
│ 4. 수모D (#b35689)  │  similarity: 6.45
│ 5. 수모E (#a04370)  │  similarity: 7.89
└─────────────────────┘
```

### 3. 전체 사용자 플로우

```
1. 사용자가 수영복 선택
         │
         ▼
2. Spring Boot가 수영복 정보 조회 (DB)
         │
         ▼
3. FastAPI에 추천 요청 (수영복 색상 전달)
         │
         ▼
4. FastAPI가 색상 유사도 계산
         │
         ▼
5. Spring Boot가 추천 수모 정보 조회 (DB)
         │
         ▼
6. 사용자에게 Top-5 수모 추천 결과 반환
```
## 🛠️ 기술 스택

### Backend

**Spring Boot 3.5.9** (비즈니스 로직 계층)
- `Spring Data JPA` - 데이터 영속성 관리
- `Spring Web MVC` - REST API 및 웹 컨트롤러
- `Spring Security` - 인증 및 권한 관리
- `Thymeleaf` - 서버 사이드 템플릿 엔진
- `MapStruct 1.5.5` - DTO ↔ Entity 매핑
- `Lombok` - 보일러플레이트 코드 감소
- `MySQL 8.0` - 관계형 데이터베이스
- `H2` - 테스트용 인메모리 DB

**FastAPI 0.127.0** (AI/ML 처리 계층)
- `YOLOv8 (Ultralytics)` - 객체 탐지 및 세그멘테이션
- `OpenCV` - 이미지 처리 및 색공간 변환
- `scikit-learn` - K-means 클러스터링
- `pyciede2000` - CIEDE2000 색상 유사도 계산
- `Selenium` - 동적 웹 크롤링
- `Pillow` - 이미지 로딩 및 변환
- `NumPy` - 수치 연산 및 배열 처리
- `SQLAlchemy` - Python ORM
- `PyMySQL` - MySQL 드라이버

### DevOps & Infrastructure

- `GitHub Actions` - CI/CD 파이프라인
- `Docker` - 컨테이너화
- `Amazon ECR` - 컨테이너 이미지 레지스트리
- `AWS App Runner` - 서버리스 컨테이너 배포
- `Gradle` - Java 빌드 도구
- `pip` - Python 패키지 관리

### AI/ML

- `YOLOv8n-seg` - 경량화된 세그멘테이션 모델
  - 수영복 탐지 모델 (커스텀 학습)
  - 수모 탐지 모델 (커스텀 학습)
- `K-means (n=3)` - 색상 클러스터링
- `CIEDE2000` - 색상 차이 계산 알고리즘

## 📊 데이터베이스 설계

```sql
-- 수영복 테이블
CREATE TABLE swimsuit (
  id VARCHAR(255) PRIMARY KEY,
  brand VARCHAR(100),
  name VARCHAR(255),
  price INT,
  product_url TEXT,
  img_url TEXT,
  is_sold_out BOOLEAN,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 수영복 색상 팔레트 (1:N 관계)
CREATE TABLE swimsuit_palette (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  swimsuit_id VARCHAR(255),
  colors VARCHAR(7),  -- HEX 색상 코드 (#e3a1ca)
  FOREIGN KEY (swimsuit_id) REFERENCES swimsuit(id) ON DELETE CASCADE
);

-- 수모 테이블
CREATE TABLE swimcap (
  id VARCHAR(255) PRIMARY KEY,
  brand VARCHAR(100),
  name VARCHAR(255),
  price INT,
  product_url TEXT,
  img_url TEXT,
  is_sold_out BOOLEAN,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 수모 색상 팔레트 (1:N 관계)
CREATE TABLE swimcap_palette (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  swimcap_id VARCHAR(255),
  colors VARCHAR(7),
  FOREIGN KEY (swimcap_id) REFERENCES swimcap(id) ON DELETE CASCADE
);
```

### ERD 관계

```
┌──────────────┐         ┌──────────────────────┐
│  swimsuit    │ 1     N │ swimsuit_palette     │
│──────────────│─────────│──────────────────────│
│ id (PK)      │         │ id (PK)              │
│ brand        │         │ swimsuit_id (FK)     │
│ name         │         │ colors               │
│ price        │         └──────────────────────┘
│ product_url  │
│ img_url      │
│ is_sold_out  │
└──────────────┘

┌──────────────┐         ┌──────────────────────┐
│  swimcap     │ 1     N │ swimcap_palette      │
│──────────────│─────────│──────────────────────│
│ id (PK)      │         │ id (PK)              │
│ brand        │         │ swimcap_id (FK)      │
│ name         │         │ colors               │
│ price        │         └──────────────────────┘
│ product_url  │
│ img_url      │
│ is_sold_out  │
└──────────────┘
```

## 🎯 핵심 알고리즘 상세

### 1. YOLOv8 기반 객체 추출

**선택 이유**
- Segmentation 모드를 사용하면 수영복/수모의 정확한 윤곽선 추출 가능
- 바운딩 박스가 아닌 픽셀 단위 마스킹으로 배경 완전 제거
- 실시간 처리가 가능한 경량 모델 (YOLOv8n)

**처리 과정**
```python
# 1. YOLO 추론
results = model(image)

# 2. 가장 신뢰도 높은 객체 선택
best_detection = max(boxes, key=lambda x: x.conf)

# 3. 마스크 추출 및 크기 조정
mask = results.masks[best_detection].cpu().numpy()
mask = cv2.resize(mask, (width, height))

# 4. 원본 이미지에 마스크 적용 (배경 제거)
swimsuit_only = cv2.bitwise_and(image, mask)
```

### 2. K-means 색상 추출

**처리 과정**
```python
# 1. 이미지 픽셀을 1차원 배열로 변환
pixels = image.reshape(-1, 3)

# 2. 극단적인 밝기 제거 (노이즈 필터링)
brightness = np.mean(pixels, axis=1)
mask = (brightness > 25) & (brightness < 230)
pixels_filtered = pixels[mask]

# 3. K-means 클러스터링 (K=3)
kmeans = KMeans(n_clusters=3, random_state=42)
kmeans.fit(pixels_filtered)

# 4. 중심점(대표 색상) 추출
colors = kmeans.cluster_centers_  # RGB 값
```

### 3. CIEDE2000 색상 유사도

**선택 이유**
- RGB 유클리드 거리는 인간의 색각 특성을 반영하지 못함
- LAB 색공간은 인간이 인지하는 색상 차이와 유사하게 설계됨
- CIEDE2000은 LAB 색공간에서 더 정확한 색상 차이 계산

**색공간 변환**
```python
# HEX → RGB → LAB 변환
def hex_to_lab(hex_color):
    # 1. HEX → RGB
    rgb = tuple(int(hex_color[i:i+2], 16) for i in (0, 2, 4))
    
    # 2. RGB → LAB (OpenCV)
    rgb_normalized = np.array(rgb) / 255.0
    lab = cv2.cvtColor(rgb_normalized, cv2.COLOR_RGB2LAB)
    
    return lab  # [L, a, b]
```

**유사도 계산**
```python
# CIEDE2000 거리 계산
def color_similarity(suit_lab, cap_lab):
    result = ciede2000(suit_lab, cap_lab)
    return result['delta_E_00']  # 작을수록 유사

# 임계값 필터링
if distance < 9.0:  # 충분히 유사한 색상만
    recommendations.append(...)
```

**CIEDE2000 vs RGB 거리 비교**
```
색상 A: #ff0000 (빨강)
색상 B: #ff3300 (주황빨강)
색상 C: #00ff00 (초록)

RGB 유클리드 거리:
- A-B: √((255-255)² + (0-51)² + (0-0)²) = 51
- A-C: √((255-0)² + (0-255)² + (0-0)²) = 360

CIEDE2000 거리:
- A-B: 12.5 (시각적으로 비슷함)
- A-C: 86.3 (시각적으로 매우 다름)

→ RGB 거리는 수치상 차이만 계산
→ CIEDE2000은 사람이 느끼는 차이를 반영
```

## 🗂️ 프로젝트 디렉토리 구조

```
swimcolorProject/
├── fastapi/                           # Python AI/ML 서비스
│   ├── app/
│   │   ├── main.py                   # FastAPI 애플리케이션 진입점
│   │   ├── config.py                 # 환경 설정
│   │   ├── schemas/                  # Pydantic 스키마
│   │   ├── db/                       # 데이터베이스 연결
│   │   └── services/
│   │       ├── crawler_service.py    # 크롤링 통합 서비스
│   │       ├── color_extractor.py    # YOLO + K-means 색상 추출
│   │       ├── similarity_service.py # CIEDE2000 유사도 계산
│   │       ├── swimwear_crawler.py   # 수영복 크롤러
│   │       └── swimcap_crawler.py    # 수모 크롤러
│   ├── ml/
│   │   ├── runs/segment/             # YOLOv8 학습 결과
│   │   │   ├── swimsuit-seg2/       # 수영복 모델
│   │   │   │   └── weights/best.pt
│   │   │   └── swimcap-seg/         # 수모 모델
│   │   │       └── weights/best.pt
│   │   ├── train_swimsuit_model.py   # 수영복 모델 학습
│   │   └── train_swimcap_model.py    # 수모 모델 학습
│   ├── test/                         # 테스트 코드
│   ├── requirements.txt
│   ├── Dockerfile
│   └── .env
│
└── spring-boot/                       # Java 비즈니스 로직
    ├── src/main/java/com/swimcolor/
    │   ├── SwimcolorApplication.java
    │   ├── controller/
    │   │   ├── api/                  # REST API 컨트롤러
    │   │   │   ├── SwimsuitApiController.java
    │   │   │   └── AdminApiController.java
    │   │   └── web/                  # 웹 페이지 컨트롤러
    │   │       ├── HomeController.java
    │   │       ├── SwimsuitController.java
    │   │       └── AdminController.java
    │   ├── service/
    │   │   ├── SwimsuitService.java
    │   │   └── RecommendationService.java  # FastAPI 호출
    │   ├── repository/               # JPA Repository
    │   │   ├── SwimsuitRepository.java
    │   │   └── SwimcapRepository.java
    │   ├── domain/                   # JPA Entity
    │   │   ├── Swimsuit.java
    │   │   ├── Swimcap.java
    │   │   └── Palette.java
    │   ├── dto/                      # 데이터 전송 객체
    │   ├── mapper/                   # MapStruct 매퍼
    │   ├── client/                   # 외부 API 클라이언트
    │   └── config/                   # 설정 클래스
    ├── src/main/resources/
    │   ├── application-local.properties
    │   ├── application-prod.properties
    │   ├── schema-mysql.sql
    │   └── templates/                # Thymeleaf 템플릿
    ├── build.gradle
    └── Dockerfile
```

## 🔍 주요 특징

### 1. 정밀한 객체 추출
- YOLOv8 Segmentation을 통한 픽셀 단위 마스킹
- 배경, 그림자, 반사광 등 노이즈 완전 제거
- 수영복/수모의 실제 색상만 정확하게 추출

### 2. 과학적 색상 매칭
- CIEDE2000 알고리즘으로 인간의 색각 특성 반영
- LAB 색공간에서 정확한 색상 거리 계산
- 임계값 기반 필터링으로 추천 품질 보장

### 3. 확장 가능한 아키텍처
- MSA 구조로 각 서비스 독립적 배포/스케일링
- RESTful API로 프론트엔드 자유롭게 연동 가능
- 새로운 추천 알고리즘 추가 용이

### 4. 실시간 데이터 수집
- Selenium 기반 동적 웹 크롤링
- 최신 상품 정보 자동 업데이트
- 품절 여부, 가격 변동 추적 가능

## 🚀 데모 및 배포

- **프로덕션 URL**: https://bit.ly/swimcolor-project
- **배포 환경**: AWS App Runner (서버리스 컨테이너)
- **CI/CD**: GitHub Actions → ECR → App Runner
- **자동 스케일링**: 트래픽에 따라 자동 확장/축소

## 📈 향후 계획

- 실제 서비스 런칭 및 사용자 피드백 수집
- Instagram 계정 운영을 통한 수영복/수모 추천 콘텐츠 제공
- 사용자 선호도 기반 개인화 추천 기능 추가
- 객체지향 설계 원칙 강화 및 코드 리팩토링
- 다양한 색상 조화 이론 적용 (보색, 유사색, 삼원색)
- 패턴/스타일 기반 추천 시스템 확장

## 📄 라이선스

이 프로젝트는 개인 포트폴리오 프로젝트입니다.

## 📧 문의

프로젝트 관련 문의사항이나 피드백은 GitHub Issues를 통해 남겨주세요.