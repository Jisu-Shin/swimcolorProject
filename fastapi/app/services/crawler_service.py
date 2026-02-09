from app.config import settings
from app.crawlers.crawler_factory import CrawlerFactory
from app.extractors.extractor_factory import ExtractorFactory

async def crawl_swimsuit_and_extract_colors(url):
    crawler = CrawlerFactory.create('ganaswim')
    products = crawler.crawl(url)

    # 1. 크롤링 결과가 없는 경우 예외 처리 (방어 코드)
    if not products:
        return []

    print(f"✅ {len(products)}건 크롤링 완료. 분석 시작...")
    extractor = ExtractorFactory.create('ver1', settings.swimsuit_onnx_path)

    try:
        imgUrList = [product['img_url'] for product in products]
        all_colors_results = await extractor.extract_colors(
            image_urls=imgUrList
        )

        for product, colors in zip(products, all_colors_results):
            if isinstance(colors, Exception):
                print(f"❌ 색상 추출 실패 ({product.get('name', 'Unknown')})")
                product['colors'] = []
            else:
                product['colors'] = [color['hex'] for color in colors]

    except Exception as e:
        print(f"❌ 상품과 색상 매핑 중  오류 발생:  {e}")
        raise e

    return products

async def crawl_swimcap_and_extract_colors(url):
    crawler = CrawlerFactory.create('ganaswim')
    products = crawler.crawl(url)

    # 1. 크롤링 결과가 없는 경우 예외 처리 (방어 코드)
    if not products:
        return []

    print(f"✅ {len(products)}건 크롤링 완료. 분석 시작...")

    extractor = ExtractorFactory.create('ver1', settings.swimcap_onnx_path)

    try :
        imgUrList = [product['img_url'] for product in products]
        all_colors_results = await extractor.extract_colors(
                    image_urls=imgUrList
        )

        for product, colors in zip(products, all_colors_results):
            if isinstance(colors, Exception):
                print(f"❌ 색상 추출 실패 ({product.get('name', 'Unknown')})")
                product['colors'] = []
            else:
                product['colors'] = [color['hex'] for color in colors]

    except Exception as e:
        print(f"❌ 상품과 색상 매핑 중  오류 발생:  {e}")
        raise e

    return products


