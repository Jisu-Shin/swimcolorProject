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

    # --- 청크(Chunk) 처리 로직 시작 ---
    chunk_size = 10  # 메모리 안전을 위해 10개씩 끊어서 처리
    all_colors_results = []

    for i in range(0, len(products), chunk_size):
        chunk = products[i: i + chunk_size]
        print(f"📦 [{i + 1}~{min(i + chunk_size, len(products))}] 번째 상품 분석 중...")

        tasks = []
        for product in chunk:
            func = partial(
                extractor.process_swimsuit_image,
                image_source=product['img_url'],
                n_colors=settings.default_n_colors,
                conf_threshold=settings.default_conf_threshold,
                visualize=False
            )
            # 엔드포인트에서 정의한 전역 executor를 써도 되고,
            # None(기본 쓰레드풀)을 써도 이미 max_workers=1로 조절했으니 안전해!
            tasks.append(loop.run_in_executor(None, func))

        # 현재 청크(10개)가 다 끝날 때까지 기다림
        chunk_results = await asyncio.gather(*tasks, return_exceptions=True)
        all_colors_results.extend(chunk_results)

        # (선택) 각 청크 사이에 0.5초 정도 쉬어주면 메모리 해제에 더 도움이 돼
        await asyncio.sleep(0.5)
    # --- 🚀 청크 처리 로직 끝 ---

    # 4. 결과 매핑
    for product, colors in zip(products, all_colors_results):
        if isinstance(colors, Exception):
            print(f"❌ 색상 추출 실패 ({product.get('name', 'Unknown')})")
            product['colors'] = []
        else:
            product['colors'] = [color['hex'] for color in colors]

    return products


