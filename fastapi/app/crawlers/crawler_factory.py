from app.crawlers.ganaswim_crawler import GanaswimCrawler

class CrawlerFactory:
    _crawlers = {
        'ganaswim': GanaswimCrawler
    }

    @classmethod
    def create(cls, crawler_type):
        crawler_class = cls._crawlers.get(crawler_type)
        print(f"현재 사용하는 크롤러 : {crawler_class}")
        if not crawler_class:
            raise ValueError(f"지원하지 않는 크롤러: {crawler_type}")
        return crawler_class()

    @classmethod
    def get_available_crawlers(cls):
        return list(cls._crawlers.keys())