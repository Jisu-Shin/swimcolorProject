from abc import ABC, abstractmethod

# 1. ABC로 인터페이스 정의
class BaseCrawler(ABC):
    @abstractmethod
    def crawl(self, url):
        """모든 크롤러가 반드시 구현해야 하는 메서드"""
        pass