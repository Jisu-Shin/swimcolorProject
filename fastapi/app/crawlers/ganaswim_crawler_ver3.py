from playwright.async_api import async_playwright, TimeoutError as PlaywrightTimeoutError
from dotenv import load_dotenv
import logging
import os
from urllib.parse import urljoin
import time
import asyncio
from typing import List, Dict, Optional

from app.crawlers.base_crawler import BaseCrawler

logger = logging.getLogger(__name__)


class GanaswimCrawlerV3(BaseCrawler):
    """가나스윔 사이트 크롤러 클래스 - Playwright 버전"""

    def __init__(self, headless=True, max_concurrent_pages=3):
        """
        Args:
            headless: True면 브라우저 창을 띄우지 않음 (백그라운드 실행)
            max_concurrent_pages: 동시에 크롤링할 페이지 수
        """
        self.headless = headless
        self.max_concurrent_pages = max_concurrent_pages
        self.product_list = []
        load_dotenv()

    async def setup_browser(self):
        """Playwright 브라우저 설정 및 실행"""
        playwright = await async_playwright().start()

        browser = await playwright.chromium.launch(
            headless=self.headless,
            args=[
                '--no-sandbox',
                '--disable-dev-shm-usage',
                '--disable-gpu',
                '--disable-cache',
                '--disk-cache-size=0',
                '--disable-background-timer-throttling',
                '--disable-backgrounding-occluded-windows',
                '--disable-renderer-backgrounding',
                '--disable-extensions',
                '--disable-plugins',
            ]
        )

        return playwright, browser

    async def crawl_single_page(self, browser, clean_url: str, page_number: int) -> List[Dict]:
        """단일 페이지 크롤링 (비동기 처리용)"""
        context = None
        page = None

        try:
            # 각 페이지마다 독립적인 컨텍스트 생성
            context = await browser.new_context(
                viewport={'width': 1920, 'height': 1080},
                user_agent='Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
            )

            # 이미지 및 스타일시트 차단으로 속도 향상
            await context.route("**/*.{png,jpg,jpeg,gif,svg,css,woff,woff2}", lambda route: route.abort())

            page = await context.new_page()

            full_url = f"{clean_url}&pageNumber={page_number}"
            logger.info(f"##### 현재 URL: {full_url}")

            # 페이지 이동
            await page.goto(full_url, wait_until='domcontentloaded', timeout=30000)

            # 상품 목록이 로드될 때까지 대기
            await page.wait_for_selector('.cGXxzj', timeout=60000)
            logger.info(f"##### 페이지 {page_number} 로딩 완료")

            # 상품 요소들 가져오기
            elements = await page.query_selector_all('.cGXxzj')
            logger.info(f"##### 페이지 {page_number}: {len(elements)}개 상품 발견")

            # 상품 정보 추출
            products = []
            for element in elements:
                product_info = await self.extract_product_info_playwright(element)
                if product_info:
                    products.append(product_info)

            return products

        except PlaywrightTimeoutError as e:
            await page.screenshot(path=f"debug_{page_number}.png")
            logger.error(f"⏱️ 페이지 {page_number} 타임아웃: {e}")
            raise Exception(f"⏱️ 페이지 {page_number} 타임아웃: {e}")
        except Exception as e:
            logger.exception(f"❌ 페이지 {page_number} 크롤링 실패: {e}")
            raise Exception(f"❌ 페이지 {page_number} 크롤링 실패: {e}")
        finally:
            if page:
                await page.close()
            if context:
                await context.close()
            logger.info(f"✓ 페이지 {page_number} 리소스 정리 완료")

    async def extract_product_info_playwright(self, element) -> Optional[Dict]:
        """
        Playwright 요소에서 정보를 추출
        """
        try:
            base_url = "https://swim.co.kr"

            # 1. 링크 추출
            link_element = await element.query_selector('a')
            product_url = ""
            if link_element:
                href = await link_element.get_attribute('href')
                product_url = urljoin(base_url, href) if href else ""

            # 2. 브랜드 추출
            brand_element = await element.query_selector('.dVHoSm')
            brand = await brand_element.inner_text() if brand_element else "알 수 없음"
            brand = brand.strip()

            # 3. 상품명 추출
            name_element = await element.query_selector('.cjytLO')
            name = await name_element.inner_text() if name_element else "상품명 없음"
            name = name.strip()

            # 4. 가격 추출
            price_elements = await element.query_selector_all('.dVHoSm')
            price = "0"
            if price_elements:
                raw_price = await price_elements[-1].inner_text()
                price = "".join(filter(str.isdigit, raw_price.strip()))

            # 5. 이미지 URL 추출
            img_element = await element.query_selector('img')
            img_url = ""
            if img_element:
                src = await img_element.get_attribute('src')
                img_url = urljoin(base_url, src) if src else ""

            # 6. 품절 여부 확인
            sold_out_element = await element.query_selector('.sc-eef3f2e7-3')
            is_sold_out = sold_out_element is not None

            return {
                "brand": brand,
                "name": name,
                "price": price,
                "product_url": product_url,
                "img_url": img_url,
                "is_sold_out": is_sold_out
            }

        except Exception as e:
            logger.debug(f"상품 정보 추출 중 건너뜀: {e}")
            return None

    async def get_end_page(self, browser) -> int:
        """마지막 페이지 번호 확인"""
        context = None
        page = None

        try:
            context = await browser.new_context(
                viewport={'width': 1920, 'height': 1080}
            )
            page = await context.new_page()

            # 임시 URL로 이동하여 마지막 페이지 확인
            await page.goto(self.temp_url, wait_until='domcontentloaded', timeout=30000)

            # 페이지네이션 버튼 대기
            await page.wait_for_selector(".sc-b97ceab4-0 button", timeout=15000)

            # 마지막 페이지 버튼 클릭
            buttons = await page.query_selector_all(".sc-b97ceab4-0 button")
            if buttons:
                last_button = buttons[-1]
                await last_button.click()

                # 페이지 로딩 대기
                await page.wait_for_selector(".sc-b97ceab4-2", timeout=10000)

                # 마지막 페이지 번호 추출
                page_elements = await page.query_selector_all(".sc-b97ceab4-2 button span")
                if page_elements:
                    last_page_text = await page_elements[-1].inner_text()
                    return int(last_page_text)

            return 1

        except Exception as e:
            logger.error(f"마지막 페이지 확인 실패: {e}")
            return 1
        finally:
            if page:
                await page.close()
            if context:
                await context.close()

    async def crawl_async(self, url: str) -> List[Dict]:
        """비동기 크롤링 실행 (메인 메서드)"""
        logger.info("🚀 Playwright 크롤링 시작...")
        start_time = time.time()

        url_arr = url.split("&pageNumber=")
        clean_url = url_arr[0]
        start_page = int(url_arr[1]) if len(url_arr) > 1 else 1

        logger.info(f"정제된 URL: {clean_url}")
        logger.info(f"시작 페이지: {start_page}")

        playwright = None
        browser = None

        try:
            # 브라우저 설정
            playwright, browser = await self.setup_browser()

            # 마지막 페이지 확인
            self.temp_url = url
            end_page = await self.get_end_page(browser)
            logger.info(f"마지막 페이지: {end_page}")

            # 크롤링할 페이지 목록 생성
            page_list = list(range(start_page, end_page + 1))
            logger.info(f"📄 총 {len(page_list)}개 페이지 크롤링 예정")

            # 페이지를 배치로 나누어 동시 처리
            for i in range(0, len(page_list), self.max_concurrent_pages):
                batch = page_list[i:i + self.max_concurrent_pages]
                logger.info(f"🔄 배치 처리 중: 페이지 {batch}")

                # 배치 내 페이지들을 동시에 크롤링
                tasks = [
                    self.crawl_single_page(browser, clean_url, page_num)
                    for page_num in batch
                ]
                results = await asyncio.gather(*tasks)

                # 결과 취합
                for page_products in results:
                    self.product_list.extend(page_products)

            total_time = time.time() - start_time
            logger.info(f"\n✅ 크롤링 완료 소요시간: {total_time:.3f}s")
            logger.info(f"📊 총 {len(self.product_list)}개 상품 수집")

            return self.product_list

        except Exception as e:
            logger.exception(f"❌ 크롤링 중 오류 발생: {e}")
            raise e
        finally:
            if browser:
                await browser.close()
            if playwright:
                await playwright.stop()

    def crawl(self, url: str) -> List[Dict]:
        """동기 래퍼 메서드 - 기존 인터페이스 호환성 유지"""
        return asyncio.run(self.crawl_async(url))


# 사용 예시
if __name__ == "__main__":
    # 로깅 설정
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )

    # 크롤러 생성
    crawler = GanaswimCrawlerV3(headless=True, max_concurrent_pages=3)

    # 크롤링 실행
    url = "https://swim.co.kr/categories/918698/products?childCategoryNo=919173&brands=%255B43160588%252C43160568%255D&pageNumber=1"
    product_list = crawler.crawl(url)

    # 결과 출력
    print(f"\n총 {len(product_list)}개 상품 수집됨")
    for i, product in enumerate(product_list[:5], 1):  # 처음 5개만 출력
        print(f"{i}. {product}")

# 다른 브랜드 URL 예시:
# 배럴: https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160576%255D&pageNumber=1
# 피닉스: https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160578%255D