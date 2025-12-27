import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch, MagicMock
from app.main import app


@pytest.fixture
def client():
    """TestClient 픽스처"""
    return TestClient(app)


@pytest.fixture
def mock_crawl_result():
    """크롤링 결과 mock 데이터"""
    return [
        {
            "name": "Test Swimsuit 1",
            "price": "50000",
            "img_url": "https://example.com/image1.jpg",
            "colors": ["#FF0000", "#00FF00", "#0000FF"]
        },
        {
            "name": "Test Swimsuit 2",
            "price": "60000",
            "img_url": "https://example.com/image2.jpg",
            "colors": ["#FFFFFF", "#000000"]
        }
    ]


class TestRootEndpoints:
    """루트 엔드포인트 테스트"""
    
    def test_root(self, client):
        """루트 엔드포인트 테스트"""
        response = client.get("/")
        assert response.status_code == 200
        data = response.json()
        assert data["message"] == "Swimcolor API"
        assert data["version"] == "1.0.0"
        assert data["docs"] == "/docs"
    
    def test_health_check(self, client):
        """헬스체크 엔드포인트 테스트"""
        response = client.get("/health")
        assert response.status_code == 200
        assert response.json() == {"status": "healthy"}


class TestCrawlEndpoint:
    """크롤링 엔드포인트 테스트"""
    
    @patch('app.main.crawl_and_extract_colors')
    def test_crawl_success(self, mock_crawl_func, client, mock_crawl_result):
        """크롤링 성공 케이스 테스트"""
        # Mock 설정
        mock_crawl_func.return_value = mock_crawl_result
        
        # 테스트 요청
        test_url = "https://example.com/swimwear"
        response = client.post("/crawl", json={"url": test_url})
        
        # 검증
        assert response.status_code == 200
        data = response.json()
        assert "products" in data
        assert len(data["products"]) == 2
        assert data["products"][0]["name"] == "Test Swimsuit 1"
        assert data["products"][0]["colors"] == ["#FF0000", "#00FF00", "#0000FF"]
        
        # Mock 함수가 올바른 인자로 호출되었는지 확인
        mock_crawl_func.assert_called_once_with(test_url)
    
    @patch('app.main.crawl_and_extract_colors')
    def test_crawl_with_empty_result(self, mock_crawl_func, client):
        """빈 결과 반환 케이스 테스트"""
        # Mock 설정 - 빈 리스트 반환
        mock_crawl_func.return_value = []
        
        # 테스트 요청
        response = client.post("/crawl", json={"url": "https://example.com/empty"})
        
        # 검증
        assert response.status_code == 200
        data = response.json()
        assert data["products"] == []
    
    @patch('app.main.crawl_and_extract_colors')
    def test_crawl_with_exception(self, mock_crawl_func, client):
        """크롤링 중 예외 발생 케이스 테스트"""
        # Mock 설정 - 예외 발생
        mock_crawl_func.side_effect = Exception("크롤링 실패")
        
        # 테스트 요청
        response = client.post("/crawl", json={"url": "https://example.com/error"})
        
        # 검증
        assert response.status_code == 500
        data = response.json()
        assert "detail" in data
        assert "크롤링 실패" in data["detail"]
    
    def test_crawl_without_url(self, client):
        """URL 없이 요청하는 경우 테스트"""
        response = client.post("/crawl", json={})
        
        # Pydantic 검증 실패로 422 에러 반환
        assert response.status_code == 422
    
    def test_crawl_with_invalid_json(self, client):
        """잘못된 JSON 형식 테스트"""
        response = client.post(
            "/crawl",
            data="invalid json",
            headers={"Content-Type": "application/json"}
        )
        
        assert response.status_code == 422
    
    @patch('app.main.crawl_and_extract_colors')
    def test_crawl_with_various_urls(self, mock_crawl_func, client, mock_crawl_result):
        """다양한 URL 형식 테스트"""
        mock_crawl_func.return_value = mock_crawl_result
        
        test_urls = [
            "https://www.example.com/products",
            "http://shop.example.com/swimwear?category=all",
            "https://example.com/products#section1"
        ]
        
        for url in test_urls:
            response = client.post("/crawl", json={"url": url})
            assert response.status_code == 200
            assert "products" in response.json()


class TestCrawlIntegration:
    """크롤링 통합 테스트 (실제 함수 호출)"""
    
    @pytest.mark.integration
    @patch('app.services.crawler_service.SwimwearCrawler')
    @patch('app.services.crawler_service.ColorExtractor')
    def test_crawl_integration(self, mock_extractor_class, mock_crawler_class, client):
        """실제 crawler_service 함수를 호출하는 통합 테스트"""
        # Mock 크롤러 설정
        mock_crawler = MagicMock()
        mock_crawler.crawl.return_value = [
            {
                "name": "Integration Test Swimsuit",
                "price": "45000",
                "img_url": "https://example.com/test.jpg"
            }
        ]
        mock_crawler_class.return_value = mock_crawler
        
        # Mock 색상 추출기 설정
        mock_extractor = MagicMock()
        mock_extractor.process_swimsuit_image.return_value = [
            {"hex": "#FF5733"},
            {"hex": "#33FF57"}
        ]
        mock_extractor_class.return_value = mock_extractor
        
        # 테스트 요청
        response = client.post("/crawl", json={"url": "https://example.com/test"})
        
        # 검증
        assert response.status_code == 200
        data = response.json()
        assert len(data["products"]) == 1
        assert data["products"][0]["name"] == "Integration Test Swimsuit"
        assert data["products"][0]["colors"] == ["#FF5733", "#33FF57"]
        
        # Mock 함수들이 호출되었는지 확인
        mock_crawler.crawl.assert_called_once()
        mock_extractor.process_swimsuit_image.assert_called_once()
