# SwimColor

[![Demo](https://img.shields.io/badge/Demo-Live-success)](https://bit.ly/swimcolor-project)
[![AWS](https://img.shields.io/badge/AWS-App%20Runner-orange)](https://aws.amazon.com/)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.127.0-009688)](https://fastapi.tiangolo.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F)](https://spring.io/)

**컴퓨터 비전 기반 색상 유사도 계산 시스템**

실시간 크롤링한 수영복/수모 이미지에서 YOLOv8 Segmentation으로 객체를 추출하고, CIEDE2000 알고리즘을 활용해 색상 유사도 기반 추천을 제공하는 프로젝트입니다.

## Overview

온라인 쇼핑 환경에서 수영복과 수모의 색상 조합을 사전에 시뮬레이션하기 어려운 문제를 해결하기 위해, 인간 시각 특성을 반영한 CIEDE2000 알고리즘을 적용한 정량적 색상 매칭 시스템입니다.

**Technical Highlights**
- MSA 아키텍처로 비즈니스 로직(Spring Boot)과 AI/ML(FastAPI) 분리
- Selenium → YOLOv8 Segmentation → K-means → CIEDE2000 파이프라인
- AWS App Runner 기반 서버리스 배포 및 자동 스케일링
- GitHub Actions를 통한 CI/CD 자동화

## Tech Stack

### Backend
- **Spring Boot 3.5.9**: JPA, WebFlux, Security, Thymeleaf
- **FastAPI 0.127.0**: YOLOv8, OpenCV, scikit-learn, Selenium

### AI/ML
- **YOLOv8 Segmentation**: 객체 추출 (배경 노이즈 제거)
- **K-means Clustering**: 대표 색상 추출 (n=3)
- **CIEDE2000**: LAB 색공간 기반 색상 유사도 계산

### Infrastructure
- **AWS App Runner**: 서버리스 컨테이너 배포
- **Amazon ECR**: 컨테이너 레지스트리
- **MySQL 8.0**: 관계형 데이터베이스
- **GitHub Actions**: CI/CD 파이프라인

## System Architecture
<img src="https://github.com/Jisu-Shin/swimcolorProject/blob/main/swimcolorProject.png">


## Core Features

### 1. Web Crawling
Selenium 기반 동적 크롤링으로 swim.co.kr의 최신 상품 정보 수집

### 2. Color Extraction Pipeline
```
이미지 다운로드
  ↓
YOLOv8 Segmentation (배경 제거)
  ↓
Noise Filtering (그림자, 반사광 제거)
  ↓
K-means Clustering (대표 색상 3개 추출)
  ↓
RGB → HEX 변환
```

### 3. Color Matching
```
수영복 색상 입력
  ↓
HEX → LAB 색공간 변환
  ↓
CIEDE2000 거리 계산 (모든 수모와 비교)
  ↓
임계값 필터링 (distance < 9.0)
  ↓
Top-6 추천 반환
```

## Project Structure

```
swimcolorProject/
├── spring-boot/
│   └── src/main/java/com/swimcolor/
│       ├── controller/          # REST API, MVC
│       ├── service/             # 비즈니스 로직
│       ├── repository/          # Spring Data JPA
│       ├── domain/              # JPA Entity
│       ├── dto/                 # DTO
│       ├── mapper/              # MapStruct
│       └── client/              # FastAPI 통신
│
└── fastapi/
    └── app/
        ├── api/                 # REST API 라우터
        ├── services/            # 크롤링, 추천 알고리즘
        ├── crawlers/            # Selenium 크롤러
        ├── extractors/          # YOLOv8 + K-means
        ├── db/                  # SQLAlchemy ORM
        └── schemas/             # Pydantic
```

## API Endpoints

| Service | Endpoint | Description |
|---------|----------|-------------|
| Spring Boot | `GET /api/v1/swimsuits/{id}/recommendations` | 수영복 기반 수모 추천 (Top-5) |
| Spring Boot | `POST /api/v1/admin/crawl` | 크롤링 트리거 |
| FastAPI | `POST /crawl/swimsuits` | 크롤링 + 색상 추출 |
| FastAPI | `POST /recommend` | CIEDE2000 유사도 계산 |

## Trade-offs & Design Decisions

### 1. MSA vs Monolith

**선택**: MSA (Spring Boot + FastAPI)

**근거**
- Python ML 생태계(YOLOv8, OpenCV) 활용 필수
- 크롤링/추론 워크로드와 비즈니스 로직의 독립적 스케일링
- Spring Boot의 트랜잭션 관리와 FastAPI의 비동기 I/O 각각 최적화

**트레이드오프**
- 네트워크 레이턴시 증가 (Spring → FastAPI HTTP 호출)
- 분산 트랜잭션 관리 복잡도

### 2. YOLOv8 Segmentation vs Bounding Box

**선택**: Segmentation (픽셀 단위 마스킹)

**근거**
- 바운딩 박스는 배경 포함 → K-means 노이즈 발생
- Segmentation은 정확한 객체 윤곽선 추출 → 순수 색상만 사용

**트레이드오프**
- 추론 시간 증가 (~50ms → ~200ms)
- 모델 크기 증가 (3MB → 6MB)

### 3. CIEDE2000 vs RGB Euclidean Distance

**선택**: CIEDE2000

**근거**
- RGB 거리는 인간 색각과 불일치
- LAB 색공간은 인간 시각 특성 기반 설계
- CIE 표준 기반 "시각적 유사도" 정량화

**트레이드오프**
- 계산 복잡도 증가
- 추천 품질 향상으로 정당화

## Performance Metrics

| 항목 | 성능 |
|------|------|
| 크롤링 속도 | ~50 items/min |
| 이미지 처리 | ~2초/이미지 |
| 추천 계산 | ~0.5초 (150개 수모 대상) |
| 배경 제거 정확도 | 95%+ |

## Future Improvements

- YOLOv8 TensorRT 변환으로 추론 속도 50% 향상
- Redis 캐싱으로 반복 요청 레이턴시 감소
- 이벤트 드리븐 아키텍처(Kafka/SQS) 적용
- CQRS 패턴 도입 (읽기/쓰기 분리)

## License

이 프로젝트는 개인 포트폴리오 목적으로 제작되었습니다.

## Contact

프로젝트 관련 문의 및 피드백은 GitHub Issues를 통해 남겨주세요.
