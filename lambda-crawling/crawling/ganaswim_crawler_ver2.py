from dotenv import load_dotenv
import logging
import os
from urllib.parse import urljoin
import time
import tempfile
import shutil

from crawling.base_crawler import BaseCrawler
from concurrent.futures import ThreadPoolExecutor

logger = logging.getLogger(__name__)


class GanaswimCrawlerV2(BaseCrawler):
    """가나스윔 사이트 크롤러 클래스"""

    def __init__(self, headless=True):
        """
        Args:
            headless: True면 브라우저 창을 띄우지 않음 (백그라운드 실행)
        """
        self.headless = headless
        self.product_list = []

        load_dotenv()

    def setup_driver(self, page_number=None):
        """크롬 드라이버 설정 및 실행"""
        from selenium import webdriver
        from selenium.webdriver.chrome.service import Service

        options = webdriver.ChromeOptions()

        chrome_bin = os.getenv('CHROME_PATH')
        options.binary_location = chrome_bin

        options.add_argument('--headless=new')
        options.add_argument('--window-size=1920,1080')
        options.add_argument('--no-sandbox')
        options.add_argument('--disable-dev-shm-usage')
        options.add_argument('--disable-gpu')
        options.add_argument('--disable-cache')
        options.add_argument('--disk-cache-size=0')
        options.add_argument('--incognito')
        options.add_argument('--disable-background-timer-throttling')
        options.add_argument('--disable-backgrounding-occluded-windows')
        options.add_argument('--disable-renderer-backgrounding')
        options.add_argument('--disable-extensions')
        options.add_argument('--disable-plugins')

        prefs = {
            "profile.managed_default_content_settings.images": 2,
            "profile.managed_default_content_settings.stylesheets": 2,
            "profile.managed_default_content_settings.fonts": 2,
        }
        options.add_experimental_option("prefs", prefs)
        options.add_argument('--disable-blink-features=AutomationControlled')
        options.add_experimental_option("excludeSwitches", ["enable-automation"])
        options.add_experimental_option('useAutomationExtension', False)

        profile_dir = None

        # 🎯 페이지 번호가 있으면 고유 포트 추가
        if page_number is not None:
            profile_dir = tempfile.mkdtemp(prefix=f'chrome_{page_number}_')
            options.add_argument(f'--user-data-dir={profile_dir}')

        service = Service()  # Service 객체 생성
        driver = webdriver.Chrome(service=service, options=options)

        # 🎯 핵심: 드라이버와 프로필 경로를 같이 반환
        return driver, profile_dir

    def crawl_single_page(self, clean_url, page_number):
        """단일 페이지 크롤링 (병렬 처리용)"""
        from selenium.webdriver.common.by import By
        from selenium.webdriver.support.ui import WebDriverWait
        from selenium.webdriver.support import expected_conditions as EC
        from bs4 import BeautifulSoup

        # 🎯 독립 driver 받기
        driver, profile_dir = self.setup_driver(page_number)

        try:
            full_url = f"{clean_url}&pageNumber={page_number}"
            logger.info(f"##### 현재 URL: {full_url}")

            # 🎯 self.driver 대신 driver 사용!
            driver.get(full_url)

            WebDriverWait(driver, 15).until(
                EC.presence_of_element_located((By.CLASS_NAME, "cGXxzj"))
            )
            logger.info(f"##### 페이지 {page_number} 로딩 완료")

            # 🎯 driver 사용
            soup = BeautifulSoup(driver.page_source, 'html.parser')
            elements = soup.select('.cGXxzj')
            logger.info(f"##### 페이지 {page_number}: {len(elements)}개 상품 발견")

            products = []
            for element in elements:
                product_info = self.extract_product_info_bs4(element)
                products.append(product_info)

            return products

        except Exception as e:
            logger.exception(f"❌ 페이지 {page_number} 크롤링 실패: {e}")
            return []

        finally:
            # ⭐ 반드시 해당 스레드의 driver만 종료
            if driver:
                driver.quit()
                logger.info(f"✓ 페이지 {page_number} 드라이버 종료")
            if profile_dir:
                shutil.rmtree(profile_dir, ignore_errors=True)

    def wait_for_load(self, driver):
        """페이지 로딩 대기"""
        from selenium.webdriver.common.by import By
        from selenium.webdriver.support.ui import WebDriverWait
        from selenium.webdriver.support import expected_conditions as EC
        try:
            WebDriverWait(driver, 40).until(
                EC.presence_of_element_located((By.CLASS_NAME, 'sc-2667f19f-45'))
            )
            return True
        except Exception as e:
            logger.exception("페이지 로딩 실패: %s", e)
            return False

    def extract_product_info_bs4(self, element):
        """
        BeautifulSoup 객체(element)에서 정보를 추출하는 초고속 로직
        """
        try:
            base_url = "https://swim.co.kr"
            # 1. 링크 추출 (find_element 대신 .find나 .select 사용)
            link_tag = element.find('a')
            product_url = urljoin(base_url, link_tag['href']) if link_tag else ""

            # 2. 브랜드, 상품명 추출 (innerText 대신 .get_text())
            brand_tag = element.select_one('.dVHoSm')
            brand = brand_tag.get_text(strip=True) if brand_tag else "알 수 없음"

            name_tag = element.select_one('.cjytLO')
            name = name_tag.get_text(strip=True) if name_tag else "상품명 없음"

            # 3. 가격 추출 (복잡한 span 구조도 텍스트로 한 번에 처리 가능)
            price_span = element.select('.dVHoSm')
            if price_span[-1]:
                # 텍스트 내에서 숫자만 골라내기 (원, , 제거)
                raw_price = price_span[-1].get_text().strip()
                # 가장 뒤에 있는 숫자가 실제 가격인 경우가 많으므로 처리
                price = "".join(filter(str.isdigit, raw_price))
            else:
                price = "0"

            # 4. 이미지 URL 추출
            img_tag = element.find('img')
            img_url = urljoin(base_url, img_tag['src']) if img_tag else ""

            # 5. 품절 여부 확인 (클래스 존재 여부만 체크)
            is_sold_out = True if element.select_one('.sc-eef3f2e7-3') else False

            return {
                "brand": brand,
                "name": name,
                "price": price,
                "product_url": product_url,
                "img_url": img_url,
                "is_sold_out": is_sold_out
            }

        except Exception as e:
            # 에러 로그는 남기되 전체 루프가 깨지지 않게 처리
            logger.debug(f"상품 정보 추출 중 건너뜀: {e}")
            return None

    def crawl_single_page(self, clean_url, page_number):
        """단일 페이지 크롤링 (병렬 처리용)"""
        from bs4 import BeautifulSoup

        # ⭐ 각 Thread마다 독립적인 driver 생성!
        driver, profile_dir = self.setup_driver(page_number)

        try:
            full_url = f"{clean_url}&pageNumber={page_number}"
            logger.info(f"##### 현재 URL: {full_url}")

            driver.get(full_url)

            # 페이지 로딩 대기
            if not self.wait_for_load(driver):
                # 여기서 바로 에러를 던지면 finally로 가서 드라이버 끄고 끝남!
                raise Exception(f"페이지 로딩 실패 (URL: {full_url})")
            logger.info(f"##### 페이지 {page_number} 로딩 완료")

            # BeautifulSoup으로 파싱
            soup = BeautifulSoup(driver.page_source, 'html.parser')
            elements = soup.select('.cGXxzj')
            logger.info(f"##### 페이지 {page_number}: {len(elements)}개 상품 발견")

            # 상품 정보 추출
            products = []
            for element in elements:
                product_info = self.extract_product_info_bs4(element)
                products.append(product_info)

            return products

        except Exception as e:
            logger.exception(f"❌ 페이지 {page_number} 크롤링 실패: {e}")
            return []

        finally:
            # ⭐ 각 Thread가 끝나면 driver 종료!
            if driver:
                driver.quit()
                logger.info(f"✓ 페이지 {page_number} 드라이버 종료")

            if profile_dir:
                shutil.rmtree(profile_dir, ignore_errors=True)

    def get_end_page(self, driver):
        from selenium.webdriver.common.by import By
        from selenium.webdriver.support.ui import WebDriverWait
        from selenium.webdriver.support import expected_conditions as EC
        try:
            # 1. '마지막 페이지로 이동' 버튼 찾기 (예: 클래스명이나 텍스트로)
            # 사이트마다 다르니 개발자 도구로 확인 필요 (예: [마지막], [>>], .btn-last)
            buttons = WebDriverWait(driver, 15).until(
                EC.presence_of_all_elements_located((By.CSS_SELECTOR, ".sc-b97ceab4-0 button"))
            )

            # for i, btn in enumerate(buttons):
            #     # 각 버튼의 텍스트와 HTML 구조 추출
            #     btn_text = btn.text.strip()
            #     btn_html = btn.get_attribute('outerHTML')
            #
            #     print(f"[{i}] 버튼 텍스트: '{btn_text}'")
            #     print(f"    HTML: {btn_html}")
            #     print("-" * 50)

            # 2. 버튼 클릭
            last_button = buttons[-1]
            last_button.click()

            # 3. 클릭 후 페이지가 로딩될 때까지 잠시 대기
            # (URL이 바뀌거나 특정 요소가 나타날 때까지)
            WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.CSS_SELECTOR, ".sc-b97ceab4-2")))

            # 4. 이제 화면에 표시된 마지막 번호 추출
            # 아까 질문하신 것처럼 span 내의 텍스트를 가져오면 됩니다.
            page_elements = driver.find_elements(By.CSS_SELECTOR, ".sc-b97ceab4-2 button span")
            last_page_num = int(page_elements[-1].text)

            return last_page_num
        except Exception as e:
            print(f"마지막 페이지 확인 실패: {e}")
            return 1

    def crawl(self, url):
        """크롤링 실행 (메인 메서드)"""
        logger.info("🚀 크롤링 시작...")
        start_time = time.time()

        url_arr = url.split("&pageNumber=")
        clean_url = url_arr[0]
        start_page = int(url_arr[1])

        logger.info(f"정제된 URL: {clean_url}")
        logger.info(f"시작 페이지: {start_page}")

        # 🎯 마지막 페이지 확인 (기존 방식)
        driver, profile_dir = self.setup_driver()
        driver.get(url)
        end_page = self.get_end_page(driver)
        driver.quit()
        if profile_dir:
            shutil.rmtree(profile_dir, ignore_errors=True)

        logger.info(f"마지막 페이지: {end_page}")

        try:
            page_list = list(range(start_page, end_page + 1))
            logger.info(f"📄 총 {len(page_list)}개 페이지 크롤링 예정")

            with ThreadPoolExecutor(max_workers=3) as executor:
                from functools import partial
                crawl_func = partial(self.crawl_single_page, clean_url)
                results = executor.map(crawl_func, page_list)

                for page_products in results:
                    self.product_list.extend(page_products)

            total_time = time.time() - start_time
            logger.info(f"\n✅ 크롤링 완료 소요시간: {total_time:.3f}s")
            logger.info(f"📊 총 {len(self.product_list)}개 상품 수집")

            return self.product_list

        except Exception as e:
            logger.exception(f"❌ 크롤링 중 오류 발생: {e}")
            raise e

# 사용 예시
if __name__ == "__main__":
    # 기본 사용 (브라우저 안보임)
    crawler = GanaswimCrawlerV2(headless=True)

    url = "https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160573%255D&pageNumber=1"
    product_list = crawler.crawl(url)

    # 결과 출력
    for i, product in enumerate(product_list, 1):
        print(f"{i}. {product}")

# 배럴
# https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160576%255D&pageNumber=1

# 피닉스
# https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160578%255D
