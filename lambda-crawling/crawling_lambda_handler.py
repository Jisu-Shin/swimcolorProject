import json
import logging
import boto3
import os
from crawling.crawler_factory import CrawlerFactory

logger = logging.getLogger()
logger.setLevel(logging.INFO)

# SQS 클라이언트 생성
sqs = boto3.client('sqs', region_name='ap-northeast-2')

# Queue URL (환경변수로 관리하는 게 좋은 습관)
QUEUE_URL = os.environ.get('SQS_QUEUE_URL')

def handler(event, context):
    """
    Lambda는 동기로 크롤링 완료까지 실행
    Spring Boot에서 이 Lambda를 Event(비동기)로 호출하면
    Spring Boot는 즉시 응답받고
    Lambda는 백그라운드에서 크롤링 계속 실행
    """
    try:
        log_id = event.get('logId')
        url = event.get('url')
        callback_url = event.get('callbackUrl')

        if not url:
            return {
                'statusCode': 400,
                'body': json.dumps({'error': 'URL이 필요합니다'})
            }

        logger.info(f"크롤링 시작: {url}")

        # 1. 크롤링 실행 (동기 래퍼 사용)
        crawler = CrawlerFactory.create('ver4')
        products = crawler.crawl(url)  # async 아닌 동기 crawl() 호출

        logger.info(f"크롤링 완료: {len(products)}개 상품")

        # 2. SQS로 데이터 적재
        batch_size = 100

        for i in range(0, len(products), batch_size):
            batch = products[i:i + batch_size]
            batch_num = i // batch_size + 1
            total_batches = (len(products) + batch_size - 1) // batch_size

            logger.info(f"   SQS 배치 {batch_num}/{total_batches} 처리 중...")

            # SQS에 메시지 전송
            response = sqs.send_message(
                QueueUrl=QUEUE_URL,
                MessageBody=json.dumps({
                    "log_id": log_id,
                    "products": batch,
                    "callback_url": callback_url
                }),  # 딕셔너리를 문자열로 변환
                MessageAttributes={
                    'ContentType': {
                        'DataType': 'String',
                        'StringValue': 'image_analysis'  # 메시지 분류용 메타데이터
                    }
                }
            )

            logger.info(f"메시지 전송 완료! MessageId: {response['MessageId']}")

        return {
            'statusCode': 200,
            'body': json.dumps({
                'message': '크롤링 완료, 색상추출 시작됨',
                'count': len(products)
            }, ensure_ascii=False)
        }

    except Exception as e:
        logger.exception(f"크롤링 실패: {e}")
        return {
            'statusCode': 500,
            'body': json.dumps({'error': str(e)})
        }