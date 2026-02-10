from .ganaswim_crawler import GanaswimCrawler
from .ganaswim_crawler_ver2 import GanaswimCrawlerV2
from .ganaswim_crawler_ver3 import GanaswimCrawlerV3

class CrawlerFactory:
    _crawlers = {
        'ganaswim': GanaswimCrawler,
        'ver2': GanaswimCrawlerV2,
        'ver3': GanaswimCrawlerV3
    }

    @classmethod
    def create(cls, crawler_type):
        print(f"크롤러 타입 : {crawler_type}")
        crawler_class = cls._crawlers.get(crawler_type)
        print(f"현재 사용하는 크롤러 : {crawler_class}")
        if not crawler_class:
            raise ValueError(f"지원하지 않는 크롤러: {crawler_type}")
        return crawler_class()

    @classmethod
    def get_available_crawlers(cls):
        return list(cls._crawlers.keys())