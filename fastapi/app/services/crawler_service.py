from .swimwear_crawler import SwimwearCrawler
from .swimcap_crawler import SwimcapCrawler
from .color_extractor import ColorExtractor
from app.config import settings

def crawl_swimsuit_and_extract_colors(url):
    # products= [];
    crawler = SwimwearCrawler()
    products = crawler.crawl(url)
    # print(products[0])

    extractor = ColorExtractor()

    for product in products:
        try:
            colors = extractor.process_swimsuit_image(
                image_source=product['img_url'],
                n_colors=settings.default_n_colors,  # 상위 5개 색상
                conf_threshold=settings.default_conf_threshold,  # 탐지 임계값 (낮추면 더 많이 탐지)
                visualize=False  # 결과 시각화
            )

            product['colors'] = [color['hex'] for color in colors]
        except Exception as e:
            print(f"❌ 색상 추출 실패 ({product.get('name', 'Unknown')}): {e}")
            product['colors'] = []
            continue

    return products

def crawl_swimcap_and_extract_colors(url):
    # products= [];
    crawler = SwimcapCrawler()
    products = crawler.crawl(url)
    # print(products[0])

    # extractor = ColorExtractor()

    # for product in products:
    #     try:
    #         colors = extractor.process_swimsuit_image(
    #             image_source=product['img_url'],
    #             n_colors=settings.default_n_colors,  # 상위 5개 색상
    #             conf_threshold=settings.default_conf_threshold,  # 탐지 임계값 (낮추면 더 많이 탐지)
    #             visualize=False  # 결과 시각화
    #         )
    #
    #         product['colors'] = [color['hex'] for color in colors]
    #     except Exception as e:
    #         print(f"❌ 색상 추출 실패 ({product.get('name', 'Unknown')}): {e}")
    #         product['colors'] = []
    #         continue

    return products


