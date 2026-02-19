import json
import logging
import asyncio
import requests
from extractors.extractor_factory import ExtractorFactory

logger = logging.getLogger()
logger.setLevel(logging.INFO)

# ============================================================================
# 설정 클래스
# ============================================================================

class Config:
    """ 설정값"""
    swimsuit_onnx_path = "onnx/swimsuit-seg/best.onnx"
    swimcap_onnx_path: str = "onnx/swimcap-seg/best.onnx"

# ============================================================================

def send_to_spring(log_id: int, products: list, callback_url: str) -> bool:
    """Spring 서버로 결과 전송"""
    try:
        payload = {
            "logId": log_id,
            "products": products,
            "crawlStatus": "COMPLETED"
        }

        response = requests.post(
            callback_url,
            json=payload,  # 자동으로 Content-Type: application/json 설정
            timeout=30  # 30초 타임아웃
        )
        response.raise_for_status()  # 4xx, 5xx면 예외 발생
        logger.info(f"✅ Spring 전송 완료: {response.status_code}")
        logger.info(f"✅ respoonse: {response}")
        return True

    except requests.exceptions.Timeout:
        logger.error("❌ Spring 서버 타임아웃")
        return False
    except requests.exceptions.ConnectionError:
        logger.error("❌ Spring 서버 연결 실패")
        return False
    except requests.exceptions.HTTPError as e:
        logger.error(f"❌ Spring 서버 HTTP 오류: {e.response.status_code}")
        return False

def _send_failed_to_spring(log_id: int, callback_url: str) :
    """실패 상태를 스프링으로 전송"""
    try:
        payload = {
            "logId": log_id,
            "crawlStatus": "FAILD"
        }

        response = requests.post(
            callback_url,
            json=payload,  # 자동으로 Content-Type: application/json 설정
            timeout=30  # 30초 타임아웃
        )

    except Exception as e:
        logger.error(f"FAILED 상태 전송 실패, error: {e}")
        raise e

def handler(event, context):
    try:
        # 배치 크기 1 → Records[0] 하나만 처리
        record = event['Records'][0]
        message_id = record.get('messageId', 'unknown')

        # SQS 바디 파싱
        raw = json.loads(record['body'])
        log_id = raw['log_id']
        products = raw['products']
        callback_url = raw['callback_url']

        logger.info(f"[{message_id}] 상품 개수: {len(products)}")

        # ✅ Extractor는 records 루프 밖에서 한 번만 생성 (모델 로딩 비용이 크기 때문)
        extractor = ExtractorFactory.create('ver2', Config.swimsuit_onnx_path)

        img_url_list = [product['img_url'] for product in products]

        all_colors_results = asyncio.run(extractor.extract_colors(
            image_urls=img_url_list
        ))

        for product, colors in zip(products, all_colors_results):
            if isinstance(colors, Exception):
                logger.error(f"❌ 색상 추출 실패 ({product.get('name', 'Unknown')}): {colors}")
                product['colors'] = []
            else:
                product['colors'] = [color['hex'] for color in colors]

        logger.info(f"[{message_id}] 처리 완료 - {len(products)}개 상품")
        logger.info(f"products 확인 : {products}")

        # ✅ return 전에 Spring으로 전송
        if not send_to_spring(log_id, products, callback_url):
            _send_failed_to_spring(log_id, callback_url)

    except Exception as e:
        logger.error(f"처리 중 오류 발생: {e}")
        raise  # ✅ SQS가 재처리하도록 예외를 다시 던짐
