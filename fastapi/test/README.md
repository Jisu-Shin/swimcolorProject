# Swimcolor API 테스트

## 테스트 구조

```
test/
├── __init__.py
├── test_main.py          # main.py의 crawl 엔드포인트 테스트
└── README.md             # 이 파일
```

## 테스트 실행

### 전체 테스트 실행
```bash
pytest
```

### 특정 테스트 파일 실행
```bash
pytest test/test_main.py
```

### 특정 테스트 클래스 실행
```bash
pytest test/test_main.py::TestCrawlEndpoint
```

### 특정 테스트 함수 실행
```bash
pytest test/test_main.py::TestCrawlEndpoint::test_crawl_success
```

### 상세 출력으로 실행
```bash
pytest -v
```

### 통합 테스트만 실행
```bash
pytest -m integration
```

### 통합 테스트 제외하고 실행
```bash
pytest -m "not integration"
```

## 테스트 커버리지 확인

```bash
# 커버리지 측정
pytest --cov=app --cov-report=html

# 커버리지 리포트 보기
open htmlcov/index.html
```

## 테스트 종류

### 1. 단위 테스트 (Unit Tests)
- `TestRootEndpoints`: 루트 및 헬스체크 엔드포인트
- `TestCrawlEndpoint`: /crawl 엔드포인트의 다양한 케이스

### 2. 통합 테스트 (Integration Tests)
- `TestCrawlIntegration`: 실제 crawler_service 함수를 호출하는 통합 테스트

## 테스트 케이스 설명

### test_crawl_success
정상적인 크롤링 요청이 성공하는 경우를 테스트합니다.

### test_crawl_with_empty_result
크롤링 결과가 빈 리스트인 경우를 테스트합니다.

### test_crawl_with_exception
크롤링 중 예외가 발생하는 경우를 테스트합니다.

### test_crawl_without_url
필수 파라미터인 URL이 없는 경우를 테스트합니다.

### test_crawl_with_invalid_json
잘못된 JSON 형식으로 요청하는 경우를 테스트합니다.

### test_crawl_with_various_urls
다양한 형식의 URL로 요청하는 경우를 테스트합니다.

## 필요한 패키지

```bash
pip install pytest pytest-cov httpx
```

## 주의사항

- Mock을 사용하여 외부 의존성(크롤러, 색상 추출기)을 격리합니다
- 실제 웹사이트에 요청을 보내지 않아 빠르고 안정적입니다
- 통합 테스트는 `-m integration` 마커로 선택적으로 실행할 수 있습니다
