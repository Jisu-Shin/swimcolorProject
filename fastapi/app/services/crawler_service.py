from app.crawlers.ganaswim_crawler import GanaswimCrawler
from app.extractors.color_extractor_parallel import ColorExtractorParallel
from app.config import settings

async def crawl_swimsuit_and_extract_colors(url):
    crawler = GanaswimCrawler()
    products = crawler.crawl(url)

    # 1. 크롤링 결과가 없는 경우 예외 처리 (방어 코드)
    if not products:
        return []

    print(f"✅ {len(products)}건 크롤링 완료. 분석 시작...")
    extractor = ColorExtractorParallel(settings.yolo_model_path)

    try:
        imgUrList = [product['img_url'] for product in products]
        all_colors_results = await extractor.extract_segment_colors(
            image_urls=imgUrList,
            n_colors=settings.default_n_colors,  # 상위 3개 색상
            conf_threshold=settings.default_conf_threshold,  # 탐지 임계값 (낮추면 더 많이 탐지)
            visualize=False  # 결과 시각화
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
    crawler = GanaswimCrawler()
    products = crawler.crawl(url)

    # 1. 크롤링 결과가 없는 경우 예외 처리 (방어 코드)
    if not products:
        return []

    print(f"✅ {len(products)}건 크롤링 완료. 분석 시작...")
    extractor = ColorExtractorParallel(settings.swimcap_yolo_model_path)

    try :
        imgUrList = [product['img_url'] for product in products]
        all_colors_results = await extractor.extract_segment_colors(
                    image_urls=imgUrList,
                    n_colors=settings.default_n_colors,  # 상위 3개 색상
                    conf_threshold=settings.default_conf_threshold,  # 탐지 임계값 (낮추면 더 많이 탐지)
                    visualize=False  # 결과 시각화
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


