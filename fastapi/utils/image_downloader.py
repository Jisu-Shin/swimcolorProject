import os
import requests
from PIL import Image
from io import BytesIO
from app.crawlers.ganaswim_crawler import GanaswimCrawler


def download_swimsuit_images(save_dir: str, products: list):
    # 디렉토리 존재 확인 및 생성
    os.makedirs(save_dir, exist_ok=True)
    success_count = 0

    for idx, product in enumerate(products, 1):
        if product["img_url"]:  # URL이 있는 경우만
            image_url = str(product["img_url"])  # URL 문자열로 변환

            # 파일명 생성 (인덱스_브랜드_상품명)
            brand = product.get('brand', 'unknown')
            name = product.get('name', f'product_{idx}')[:6]
            file_name = f"{idx:04d}_{brand}_{name}.jpg"

            save_path = os.path.join(save_dir, file_name)

            try:
                resp = requests.get(image_url, timeout=10)
                resp.raise_for_status()

                # BytesIO로 메모리에서 이미지 열기
                image = Image.open(BytesIO(resp.content))

                # RGB 모드로 변환 (JPG는 RGB만 지원)
                if image.mode != 'RGB':
                    image = image.convert('RGB')

                # JPG로 저장 (품질 95%)
                image.save(save_path, 'JPEG', quality=95, optimize=True)

                success_count += 1
                print(f"✓ JPG 저장됨: {file_name}")

            except Exception as e:
                print(f"✗ 실패: {image_url} -> {e}")

    print(f"\n완료: {success_count}/{len(products)}개 저장")
    return success_count

# 사용 예시
if __name__ == "__main__":
    # 기본 사용 (브라우저 안보임)
    crawler = GanaswimCrawler(headless=True)

    # 저장 디렉토리 설정
    save_dir = '/Users/zsu/MyProject/크롤링 사진/swimcap_1228'

    url = "https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160578%255D"

    product_list = crawler.crawl(url)
    download_swimsuit_images(save_dir, product_list);
