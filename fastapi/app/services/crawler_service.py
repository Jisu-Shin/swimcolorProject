from .swimwear_crawler import SwimwearCrawler
from .swimcap_crawler import SwimcapCrawler
from .color_extractor import ColorExtractor
from app.config import settings
import asyncio
from functools import partial

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
                n_colors=settings.default_n_colors,  # 상위 3개 색상
                conf_threshold=settings.default_conf_threshold,  # 탐지 임계값 (낮추면 더 많이 탐지)
                visualize=False  # 결과 시각화
            )

            product['colors'] = [color['hex'] for color in colors]
        except Exception as e:
            print(f"❌ 색상 추출 실패 ({product.get('name', 'Unknown')}): {e}")
            product['colors'] = []
            continue

    return products

async def crawl_swimcap_and_extract_colors(url):
    crawler = SwimcapCrawler()
    products = crawler.crawl(url)

    # 1. 크롤링 결과가 없는 경우 예외 처리 (방어 코드)
    if not products:
        return []

    print(f"✅ {len(products)}건 크롤링 완료. 분석 시작...")
    extractor = ColorExtractor(settings.swimcap_yolo_model_path)
    loop = asyncio.get_running_loop()

    tasks = []
    # 2. 태스크 예약 (여기는 빛의 속도로 지나감)
    for product in products:
        func = partial(
            extractor.process_swimsuit_image,
            image_source=product['img_url'],
            n_colors=settings.default_n_colors,
            conf_threshold=settings.default_conf_threshold,
            visualize=False
        )
        tasks.append(loop.run_in_executor(None, func))

    # 3. 실제 병렬 연산 실행 (여기서 기다림)
    # return_exceptions=True를 넣으면 하나가 에러 나도 나머지는 끝까지 돌아가!
    all_colors_results = await asyncio.gather(*tasks, return_exceptions=True)

    # 4. 결과 매핑
    for product, colors in zip(products, all_colors_results):
        if isinstance(colors, Exception):
            print(f"❌ 색상 추출 실패 ({product.get('name', 'Unknown')}): {colors}")
            product['colors'] = []
        else:
            product['colors'] = [color['hex'] for color in colors]

    return products


