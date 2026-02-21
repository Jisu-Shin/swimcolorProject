from abc import ABC, abstractmethod

# 1. ABC로 인터페이스 정의
class BaseExtractor(ABC):
    @abstractmethod
    def extract_colors(self, image_urls, target_class) :
        """모든 추출기가 반드시 구현해야 하는 메서드"""
        pass