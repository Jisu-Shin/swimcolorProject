from playwright.async_api import async_playwright, TimeoutError as PlaywrightTimeoutError
from dotenv import load_dotenv
import logging
import time
import asyncio
from typing import List, Dict, Tuple
from urllib.parse import urlparse, parse_qs, urlencode, urlunparse

from crawling.base_crawler import BaseCrawler

logger = logging.getLogger(__name__)

class GanaswimCrawlerV4(BaseCrawler):
    """가나스윔 사이트 크롤러 클래스 - Playwright 순차처리 버전"""

    def __init__(self):
        self.product_list = []
        load_dotenv()

    async def setup_browser(self):
        """Playwright 브라우저 설정"""
        playwright = await async_playwright().start()

        browser = await playwright.chromium.launch(
            headless=True,
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
                '--single-process'
            ]
        )
        return playwright, browser

    async def crawl_page(self, context, full_url: str, page_number: int) -> Tuple[List[Dict], bool]:
        """
        단일 페이지 크롤링 + 다음 페이지 여부 반환
        Returns: (products, has_next)
        """
        page = None

        try:
            page = await context.new_page()

            logger.info(f"##### 현재 URL: {full_url}")
            await page.goto(full_url, wait_until='domcontentloaded', timeout=30000)
            await page.wait_for_selector('.cGXxzj', timeout=60000)
            logger.info(f"##### 페이지 {page_number} 로딩 완료")

            # 상품 추출 + 다음 페이지 여부를 JS로 한번에 처리
            result = await page.evaluate("""
                (current_page
                ) => {
                    const base_url = 'https://swim.co.kr';
                    const protocol = 'https:';

                    const elements = document.querySelectorAll('.cGXxzj');
                    const products = [...elements].map(el => {
                        const link = el.querySelector('a');
                        const brand = el.querySelector('.dVHoSm');
                        const name = el.querySelector('.cjytLO');
                        const prices = el.querySelectorAll('.dVHoSm');
                        const img = el.querySelector('img');
                        const soldOut = el.querySelector('.sc-eef3f2e7-3');
                        
                        return {
                            brand: brand ? brand.textContent.trim() : '알 수 없음',
                            name: name ? name.textContent.trim() : '상품명 없음',
                            price: prices.length ? prices[prices.length-1].textContent.replace(/[^0-9]/g, '') : '0',
                            product_url: link ? base_url + link.getAttribute('href') : '',
                            img_url: img ? protocol + img.getAttribute('src') : '',
                            is_sold_out: !!soldOut
                        };
                    });

                    // 현재 페이지 번호와 마지막 페이지 번호 비교
                    const pageButtons = document.querySelectorAll('.sc-b97ceab4-2 button span');
                    const lastPageNum = pageButtons.length ? parseInt(pageButtons[pageButtons.length - 1].textContent) : 0;
                    const has_next = lastPageNum > current_page;

                    return { products, has_next };
                }
            """, page_number)

            products = result['products']
            has_next = result['has_next']
            logger.info(f"✅ 페이지 {page_number}: {len(products)}개 수집 | 다음 페이지: {has_next}")

            return products, has_next

        except PlaywrightTimeoutError as e:
            if page:
                await page.screenshot(path=f"debug_{page_number}.png")
            logger.error(f"⏱️ 페이지 {page_number} 타임아웃: {e}")
            raise
        except Exception as e:
            logger.exception(f"❌ 페이지 {page_number} 크롤링 실패: {e}")
            raise
        finally:
            if page:
                await page.close()

    async def crawl_async(self, url: str) -> List[Dict]:
        """순차처리 크롤링 메인 메서드"""
        logger.info("🚀 Playwright 순차 크롤링 시작...")
        start_time = time.time()

        # 1. URL 분석 및 쿼리 스트링 추출
        parsed_url = urlparse(url)
        query_params = parse_qs(parsed_url.query)

        # 2. pageNumber 추출 (없으면 기본값 1)
        # parse_qs는 값을 리스트 형태로 반환하므로 [0]으로 접근합니다.
        page_number = int(query_params.get('pageNumber', [1])[0])

        # 3. pageNumber를 제외한 깨끗한 URL 생성
        # 쿼리 파라미터에서 pageNumber 삭제
        if 'pageNumber' in query_params:
            del query_params['pageNumber']

        # 다시 URL 조립
        new_query = urlencode(query_params, doseq=True)
        clean_url = urlunparse(parsed_url._replace(query=new_query))

        logger.info(f"정제된 URL: {clean_url}")
        logger.info(f"시작 페이지: {page_number}")

        playwright = None
        browser = None
        context = None

        try:
            playwright, browser = await self.setup_browser()

            context = await browser.new_context(
                viewport={'width': 1920, 'height': 1080},
                user_agent='Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
            )
            await context.route(
                "**/*.{gif,svg,css,woff,woff2}",
                lambda route: route.abort()
            )

            # 순차처리 - has_next False 될 때까지 반복
            while True:
                full_url = f"{clean_url}&pageNumber={page_number}"
                products, has_next = await self.crawl_page(context, full_url, page_number)
                self.product_list.extend(products)

                if not has_next:
                    logger.info(f"🏁 마지막 페이지 도달: {page_number}")
                    break

                page_number += 1

            total_time = time.time() - start_time
            logger.info(f"\n✅ 크롤링 완료 소요시간: {total_time:.3f}s")
            logger.info(f"📊 총 {len(self.product_list)}개 상품 수집")

            return self.product_list

        except Exception as e:
            logger.exception(f"❌ 크롤링 중 오류 발생: {e}")
            raise
        finally:
            if context:
                await context.close()
            if browser:
                await browser.close()
            if playwright:
                await playwright.stop()

    def crawl(self, url: str) -> List[Dict]:
        """동기 래퍼 - 기존 인터페이스 호환"""
        return asyncio.run(self.crawl_async(url))


if __name__ == "__main__":
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )

    crawler = GanaswimCrawlerV4()

    url = "https://swim.co.kr/categories/918698/products?childCategoryNo=919173&brands=%255B43160588%252C43160568%255D&pageNumber=3"
    product_list = crawler.crawl(url)

    print(f"\n총 {len(product_list)}개 상품 수집됨")
    for i, product in enumerate(product_list[:5], 1):
        print(f"{i}. {product}")

# 다른 브랜드 URL 예시:
# 배럴: https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160576%255D&pageNumber=1
# 피닉스: https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160578%255D