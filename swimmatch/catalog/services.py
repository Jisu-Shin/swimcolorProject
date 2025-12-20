from crawlers.swimwear_crawler import SwimwearCrawler
# from crawlers.data_processor import ColorProcessor
from .models import Swimsuit

def run_crawl_and_save(url):
    """크롤링 실행 및 DB 저장"""
    crawler = SwimwearCrawler()
    product_list = crawler.crawl(url)

    # processor = ColorProcessor()
    created_count = 0
    skipped_count = 0
    errors = []

    print(f"\n💾 DB 저장 시작...   ({len(product_list)}개)")

    for product in product_list:
        if Swimsuit.objects.filter(product_url=product['product_url']).exists():
            print(f"⊘ 이미 존재:   {product['name']}")
            skipped_count += 1
            continue

        try:
            # rgb = processor.get_dominant_color(product['img_url'])
            # color = processor.classify_color(rgb)

            Swimsuit.objects.create(
                brand=product['brand'],
                name=product['name'],
                # color=color,
                dominant_color_hex='#FFFFFF',
                # palette = [],
                palette = ['#123456','#7890123'],
                price=product['price'],
                product_url=product['product_url'],
                image=product['img_url'],
                # dominant_lab=f"{rgb[0]},{rgb[1]},{rgb[2]}" if rgb else ""
                dominant_lab=[50.25, 60.15, 30.45]
            )
            created_count += 1

        except Exception as e:
            errors.append(f"{product['name']}: {str(e)}")
            skipped_count += 1

    return {
        'created': created_count,
        'skipped': skipped_count,
        'errors': errors
    }