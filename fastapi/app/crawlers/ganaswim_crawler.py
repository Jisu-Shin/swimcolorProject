from selenium.common import NoSuchElementException
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from dotenv import load_dotenv
import logging
import os
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from urllib.parse import urljoin

logger = logging.getLogger(__name__)


class GanaswimCrawler:
    """가나스윔 사이트 크롤러 클래스"""

    def __init__(self, headless=True):
        """
        Args:
            headless: True면 브라우저 창을 띄우지 않음 (백그라운드 실행)
        """
        self.driver = None
        self.headless = headless
        self.product_list = []
        load_dotenv()

    def setup_driver(self):
        """크롬 드라이버 설정 및 실행"""
        options = webdriver.ChromeOptions()

        # 1. 브라우저(Chrome) 실행 파일 경로 설정
        # os.getenv('CHROME_PATH')가 /usr/bin/google-chrome 라면 여기에 할당합니다.
        chrome_bin = os.getenv('CHROME_PATH')
        options.binary_location = chrome_bin

        options.add_argument('--headless=new')  # 최신 헤드리스 모드 (더 빠름)
        options.add_argument('--window-size=1920,1080')  # 도커 환경과 동일하게 설정
        options.add_argument('--no-sandbox')
        options.add_argument('--disable-dev-shm-usage')
        options.add_argument('--disable-gpu')

        # [핵심] 리소스 차단: 이미지, CSS, 폰트 로딩 방지 (CPU 낭비 방지)
        prefs = {
            "profile.managed_default_content_settings.images": 2,
            "profile.managed_default_content_settings.stylesheets": 2,
            "profile.managed_default_content_settings.fonts": 2,
        }
        options.add_experimental_option("prefs", prefs)

        # 자동화 탐지 방지 (UC 대신 가벼운 옵션)
        options.add_argument('--disable-blink-features=AutomationControlled')
        options.add_experimental_option("excludeSwitches", ["enable-automation"])
        options.add_experimental_option('useAutomationExtension', False)

        # Service에는 '드라이버' 경로를 넣어야 합니다.
        service = Service()
        self.driver = webdriver.Chrome(service=service, options=options)

        # 실행 속도 향상을 위한 스크립트 실행 (Webdriver 속성 제거)
        self.driver.execute_cdp_cmd("Page.addScriptToEvaluateOnNewDocument", {
            "source": """
                    Object.defineProperty(navigator, 'webdriver', {
                        get: () => undefined
                    })
                """
        })

    def quit_driver(self):
        """드라이버 종료"""
        if self.driver:
            self.driver.quit()
            logger.info("✓ 드라이버 종료됨")

    def get_end_page(self, pageDiv):
        """마지막 페이지 번호 가져오기"""
        try:
            buttons = pageDiv.find_all('button')
            pageLastButton = buttons[-1]
            endPage = int(pageLastButton.find('span').get_text(strip=True))
            return endPage
        except Exception as e:
            logger.exception("페이지 로딩 실패: %s", e)
            return 1

    def wait_for_load(self):
        """페이지 로딩 대기"""
        try:
            WebDriverWait(self.driver, 20).until(
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

    def crawl_page(self, elements):
        """현재 페이지의 모든 상품 정보 추출"""
        try:
            logger.info(f"📦 발견된 상품 수: {len(elements)}")

            for element in elements:
                product_info = self.extract_product_info_bs4(element)
                self.product_list.append(product_info)

            return True

        except Exception as e:
            logger.exception(f"BS4 파싱 중 오류 발생: {e}")
            return False

    def crawl(self, url):
        """
        크롤링 실행 (메인 메서드)

        Args:
            url: 크롤링할 기본 URL (pageNumber 파라미터 제외)

        Returns:
            list: 추출된 상품 리스트
        """
        logger.info("🚀 크롤링 시작...")

        # 드라이버 설정
        self.setup_driver()
        self.product_list = []

        clean_url = url.split("&pageNumber=1")[0] if "&pageNumber=1" in url else url

        try:
            current_page = 1

            while True:
                logger.debug(f"\n📄 페이지 {current_page} 처리 중...")

                # URL 접속
                full_url = f"{clean_url}&pageNumber={current_page}"
                logger.info(f"##### 현재 url {full_url}")

                self.driver.get(full_url)

                # 페이지 로딩 대기
                # --- 재시도 없이 바로 체크 ---
                if not self.wait_for_load():
                    # 여기서 바로 에러를 던지면 finally로 가서 드라이버 끄고 끝남!
                    raise Exception(f"페이지 로딩 실패 (URL: {full_url})")

                # 1. 소스 가져오기 (Selenium 통신 1회)
                soup = BeautifulSoup(self.driver.page_source, 'html.parser')

                # 2. 상품 리스트 추출
                elements = soup.select('.cGXxzj')  # 마침표(.) 필수!

                # 3. 데이터 파싱 실행 (메모리 연산이라 광속!)
                if not self.crawl_page(elements):
                    break

                # 4. 마지막 페이지 확인 로직 (클래스명 선택 주의)
                pageDiv = soup.find(class_='sc-b97ceab4-2')
                if pageDiv:
                    end_page = self.get_end_page(pageDiv)
                else:
                    end_page = current_page  # 못 찾으면 현재 페이지를 마지막으로 간주

                if current_page >= end_page:
                    logger.info(f"✓ 마지막 페이지({end_page}) 도달")
                    break

                current_page += 1

        except Exception as e:
            logger.exception(f"❌ 크롤링 중 오류 발생:  {e}")

            # ⭐ 핵심: 에러를 다시 던져야 아래 return 문으로 안 내려가!
            raise e

        finally:
            self.quit_driver()

        logger.info(f"\n✅ 크롤링 완료!")
        logger.info(f"📊 총 {len(self.product_list)}개 상품 수집")

        return self.product_list


# 사용 예시
if __name__ == "__main__":
    # 기본 사용 (브라우저 안보임)
    crawler = GanaswimCrawler(headless=True)

    url = "https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160576%255D&pageNumber=1"
    product_list = crawler.crawl(url)

    # 결과 출력
    for i, product in enumerate(product_list, 1):
        print(f"{i}. {product}")

# 배럴
# https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160576%255D&pageNumber=1

# 피닉스
# https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160578%255D
