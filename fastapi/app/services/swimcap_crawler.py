from selenium. common import NoSuchElementException
from selenium. webdriver.common.by import By
from selenium. webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time
import undetected_chromedriver as uc

class SwimcapCrawler:
    """수모 크롤러 클래스"""

    def __init__(self, headless=True):
        """
        Args:
            headless: True면 브라우저 창을 띄우지 않음 (백그라운드 실행)
        """
        self.driver = None
        self.headless = headless
        self.product_list = []

    def setup_driver(self):
        """크롬 드라이버 설정 및 실행"""
        options = uc.ChromeOptions()

        if self.headless:
            options.add_argument('--headless=new')  # 브라우저 창을 띄우지 않음

        options.add_argument('--no-sandbox')
        options.add_argument('--disable-dev-shm-usage')
        options.add_argument('--window-size=1920,1080')

        self.driver = uc.Chrome(
            options=options
            , browser_executable_path="/usr/bin/google-chrome"
        )

    def quit_driver(self):
        """드라이버 종료"""
        if self.driver:
            self.driver.quit()
            print("✓ 드라이버 종료됨")

    def get_end_page(self):
        """마지막 페이지 번호 가져오기"""
        try:
            pageDiv = self.driver.find_element(By.CLASS_NAME, 'sc-b97ceab4-2')
            pageLastButton = pageDiv.find_elements(By.TAG_NAME, 'button')[-1]
            endPage = int(pageLastButton.find_element(By.TAG_NAME, 'span').text)
            return endPage
        except Exception as e:
            print(f"페이지 번호 조회 실패:  {e}")
            return 1

    def wait_for_load(self):
        """페이지 로딩 대기"""
        try:
            WebDriverWait(self.driver, 15).until(
                EC.presence_of_element_located((By.CLASS_NAME, 'sc-2667f19f-45'))
            )
            return True
        except Exception as e:
            print(f"페이지 로딩 실패: {e}")
            return False

    def extract_product_info(self, element):
        """
        개별 상품 정보 추출

        Returns:
            dict: 상품 정보 또는 None (추출 실패 시)
        """
        try:
            # 링크 추출
            product_url = element.find_element(By.TAG_NAME, 'a').get_attribute('href')

            # 상품명, 브랜드, 가격 추출
            desc = element.find_element(By.CLASS_NAME, 'kIsRDZ').text

            desc_split = desc.split('\n')
            brand = desc_split[0]
            name = desc_split[1]
            price = desc_split[2] if len(desc_split) == 3 else desc_split[3]

            price = price.replace(",", "").replace("원", "")

            # 이미지 URL 추출
            img_url = element.find_element(By.TAG_NAME, 'img').get_attribute('src')

            # 품절 여부 확인
            try:
                element.find_element(By.CLASS_NAME, 'sc-eef3f2e7-3')
                is_sold_out = True
            except NoSuchElementException:
                is_sold_out = False

            return {
                "brand": brand,
                "name": name,
                "price": price,
                "product_url": product_url,
                "img_url": img_url,
                "is_sold_out": is_sold_out
            }

        except Exception as e:
            print(f"상품 정보 추출 실패:  {e}")
            return None

    def crawl_page(self):
        """현재 페이지의 모든 상품 정보 추출"""
        try:
            elements = self.driver.find_elements(By.CLASS_NAME, 'cGXxzj')
            print(f"📦 발견된 상품 수:  {len(elements)}")

            for element in elements:
                product_info = self.extract_product_info(element)

                if product_info:
                    if product_info['is_sold_out']:
                        print(f"  ✗ [품절] {product_info['brand']} - {product_info['name']}")
                    else:
                        # print(f"  ✓ {product_info['brand']} - {product_info['name']}")
                        self.product_list.append(product_info)

            return True

        except Exception as e:
            print(f"페이지 크롤링 실패: {e}")
            return False

    def crawl(self, url):
        # print("\n\ncrawl에서 확인", url);
        """
        크롤링 실행 (메인 메서드)

        Args:
            url: 크롤링할 기본 URL (pageNumber 파라미터 제외)

        Returns:
            list: 추출된 상품 리스트
        """
        print("🚀 크롤링 시작...")

        # 드라이버 설정
        self.setup_driver()
        self.product_list = []

        try:
            current_page = 1

            while True:
                print(f"\n📄 페이지 {current_page} 처리 중...")

                # URL 접속
                full_url = f"{url}&pageNumber={current_page}"
                self.driver.get(full_url)

                # 페이지 로딩 대기
                if not self.wait_for_load():
                    print("⚠️ 페이지 로딩 실패, 재시도...")
                    time.sleep(2)
                    continue

                # 현재 페이지의 상품 크롤링
                if not self.crawl_page():
                    break

                # 마지막 페이지 확인
                end_page = self.get_end_page()

                if current_page >= end_page:
                    print(f"✓ 마지막 페이지({end_page})에 도달")
                    break

                current_page += 1
                time.sleep(1)  # 서버 부하 방지

        except Exception as e:
            print(f"❌ 크롤링 중 오류 발생:  {e}")

        finally:
            self.quit_driver()

        print(f"\n✅ 크롤링 완료!")
        print(f"📊 총 {len(self.product_list)}개 상품 수집")

        return self.product_list

# 사용 예시
if __name__ == "__main__":
    # 기본 사용 (브라우저 안보임)
    crawler = SwimcapCrawler(headless=True)

    url = "https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160576%255D&pageNumber=1"

    product_list = crawler.crawl(url)

    # 결과 출력
    for i, product in enumerate(product_list, 1):
        print(f"{i}. {product}")

# 배럴
# https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160576%255D&pageNumber=1

# 피닉스
# https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160578%255D