# Swimcolor API

수영복과 수모의 색상을 추출하고 유사한 제품을 추천하는 FastAPI 애플리케이션입니다.

## 📋 목차
- [환경 설정](#환경-설정)
- [실행 방법](#실행-방법)
- [API 엔드포인트](#api-엔드포인트)
- [프로젝트 구조](#프로젝트-구조)

---

## 🔧 환경 설정

### 1. 필수 요구사항
- Python 3.8+
- MySQL 데이터베이스
- Chrome/Chromium (크롤링용)

### 2. 의존성 설치
```bash
pip install -r requirements.txt
```

### 3. 환경 변수 설정
`.env` 파일을 생성하여 데이터베이스 정보를 입력합니다

```env
# 로그 레벨 설정
LOG_LEVEL=info

# 데이터베이스 설정 (방법 1: 개별 설정)
DB_USER=your_username
DB_PASSWORD=your_password
DB_HOST=localhost
DB_PORT=3306
DB_DATABASE=your_database

# 또는 (방법 2: URL 직접 설정)
# DATABASE_URL=mysql+pymysql://your_username:your_password@localhost:3306/your_database?charset=utf8mb4
```

> ⚠️ **주의**: `.env` 파일은 민감한 정보를 포함하므로 절대 Git에 커밋하지 마세요!

---

## 🚀 실행 방법

### 개발 서버 실행
```bash
uvicorn app.main:app --reload --port 8000
```

### 프로덕션 서버 실행
```bash
gunicorn app.main:app -w 4 -k uvicorn.workers.UvicornWorker --bind 0.0.0.0:8000
```

---

## 📡 API 엔드포인트

### 서버 상태 확인
```http
GET http://127.0.0.1:8000/
GET http://127.0.0.1:8000/health
```

### 수영복 크롤링
```http
POST http://127.0.0.1:8000/crawl/swimsuits
Content-Type: application/json

{
  "logId": 1,
  "crawlingUrl": "https://swim.co.kr/categories/918606/products",
  "callbackUrl": "http://your-spring-server/api/callback"
}
```

### 수모 크롤링
```http
POST http://127.0.0.1:8000/crawl/swimcaps
Content-Type: application/json

{
  "logId": 2,
  "crawlingUrl": "https://swim.co.kr/categories/918606/products?childCategoryNo=919019",
  "callbackUrl": "http://your-spring-server/api/callback"
}
```

### 수모 추천
```http
POST http://127.0.0.1:8000/recommend
Content-Type: application/json

{
  "swimsuitId": "SW001",
  "colors": ["#e3a1ca", "#be77a0", "#592d39"]
}
```

---

## 📁 프로젝트 구조

```
fastapi/
├── app/
│   ├── api/                    # API 라우터
│   │   ├── __init__.py
│   │   └── endpoints.py        # 엔드포인트 정의
│   ├── db/                     # 데이터베이스
│   │   ├── __init__.py
│   │   ├── connection.py       # DB 연결 설정
│   │   ├── models.py           # SQLAlchemy 모델
│   │   └── repositories.py     # 데이터 액세스 레이어
│   ├── schemas/                # Pydantic 스키마
│   ├── services/               # 비즈니스 로직
│   │   ├── crawler_service.py
│   │   └── similarity_service.py
│   ├── enums.py               # Enum 정의
│   ├── config.py              # 설정
│   └── main.py                # FastAPI 앱 진입점
├── .env                       # 환경 변수 (gitignore)
├── .env.example              # 환경 변수 템플릿
├── requirements.txt          # Python 의존성
└── README.md                 # 프로젝트 문서
```

---

## 🔍 크롤링 예제 URL

### 수모 크롤링 URL
```
https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160578%255D&pageNumber=1
```

---

## 📝 응답 예시 
```
{
  "products": [
    {
      "brand": "아디다스",
      "name": "우먼 에센셜 V백 스윔수트 아시안핏 프리러브드 퍼플",
      "price": 49000,
      "product_url": "https://swim.co.kr/products/131427390",
      "img_url": "https://swim.cdn-nhncommerce.com/Mall-No-k12B/20250821/151040.986016082/M3.jpg",
      "is_sold_out": false,
      "colors": [
        "#e3a1ca",
        "#be77a0",
        "#592d39"
      ]
    },
    {
      "brand": "아디다스",
      "name": "우먼 에센셜 V백 스윔수트 아시안핏 프리러브드 틸",
      "price": 49000,
      "product_url": "https://swim.co.kr/products/131427389",
      "img_url": "https://swim.cdn-nhncommerce.com/Mall-No-k12B/20250821/145516.788895654/M3.jpg",
      "is_sold_out": false,
      "colors": []
    },
```

## 수모 크롤링 url
```
https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160578%255D&pageNumber=1
```

## deploy test
