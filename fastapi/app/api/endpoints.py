from fastapi import APIRouter, HTTPException, Depends
from sqlalchemy import text
from sqlalchemy.orm import Session
from app.db import get_db
from app.schemas import CrawlRequest, CrawlResponse, SwimsuitRequest
from app.services.crawler_service import crawl_swimsuit_and_extract_colors, crawl_swimcap_and_extract_colors
from app.services.similarity_service import recommend_swim_caps
from app.enums import ItemType
import httpx
import asyncio
from concurrent.futures import ThreadPoolExecutor

router = APIRouter()

# 전역 변수로 쓰레드 풀 생성 (동시 작업 수 제한)
executor = ThreadPoolExecutor(max_workers=1)

@router.get("/")
async def root(db: Session = Depends(get_db)):
    """API 루트 엔드포인트 - DB 연결 상태 확인"""
    try:
        # 간단한 쿼리로 연결 확인
        result = db.execute(text("SELECT 1")).scalar()
        db_status = "OK" if result else "Error"
    except Exception as e:
        db_status = f"Error: {str(e)}"

    return {
        "message": "Swimcolor API",
        "version": "1.0.0",
        "docs": "/docs",
        "db_status": db_status
    }


@router.get("/health")
async def health_check():
    """헬스 체크 엔드포인트"""
    return {"status": "healthy"}


@router.post("/crawl/swimsuits")
async def crawl_swimsuit(request: CrawlRequest):
    """수영복 크롤링 엔드포인트"""
    print(f"#### [요청 데이터 확인]: {request}")
    asyncio.create_task(
        run_crawling_task(
            request.logId,
            request.crawlingUrl,
            request.callbackUrl,
            ItemType.SWIMSUIT
        )
    )

    # 2. Spring에게 0.1초 만에 응답 반환 (504 방지)
    return {"logId": request.logId}

@router.post("/crawl/swimcaps")
async def crawl_swimcap(request: CrawlRequest):
    """수모 크롤링 엔드포인트"""
    # 1. async 함수인 run_crawling_task를 백그라운드로 예약 (즉시 리턴 가능)
    # create_task는 비동기 함수(코루틴)를 실행할 때 사용하므로 에러 안 남!
    asyncio.create_task(
        run_crawling_task(
            request.logId,
            request.crawlingUrl,
            request.callbackUrl,
            ItemType.SWIMCAP
        )
    )

    # 2. Spring에게 0.1초 만에 응답 반환 (504 방지)
    return {"logId": request.logId}

# 백그라운드 태스크 함수
async def run_crawling_task(log_id: int, url: str, callback_url: str, item_type: ItemType):
    """
    크롤링을 백그라운드에서 수행하고 결과를 콜백 URL로 전송
    
    Args:
        log_id: 로그 ID
        url: 크롤링할 URL
        callback_url: 결과를 전송할 콜백 URL
        item_type: 아이템 타입 (SWIMSUIT 또는 SWIMCAP)
    """

    loop = asyncio.get_running_loop()

    try:
        # 3. ⭐ 핵심: 일반 함수(def)인 크롤링 로직만 executor로 격리 실행
        # 여기서 'await'를 붙여줘야 크롤링이 끝날 때까지 이 함수 내에서만 기다려
        if item_type == ItemType.SWIMCAP:
            extracted_products = await loop.run_in_executor(
                executor,
                crawl_swimcap_and_extract_colors,  # 일반 함수
                url  # 인자
            )
        elif item_type == ItemType.SWIMSUIT:
            extracted_products = await loop.run_in_executor(
                executor,
                crawl_swimsuit_and_extract_colors,  # 일반 함수
                url  # 인자
            )
        # (기타 타입 처리...)

        response_data = CrawlResponse(logId=log_id, crawlStatus="COMPLETED", errorMsg="", products=extracted_products)

    except Exception as e:
        import traceback

        traceback.print_exc()
        response_data = CrawlResponse(logId=log_id, crawlStatus="FAILED", errorMsg=str(e), products=[])

    # 4. 결과 콜백 전송 (비동기 처리)
    async with httpx.AsyncClient() as client:
        await client.post(callback_url, json=response_data.model_dump())


@router.post("/recommend")
async def recommend_swimcaps(request: SwimsuitRequest, db: Session = Depends(get_db)):
    """수모 추천 엔드포인트"""
    try:
        similarList = recommend_swim_caps(db, request.swimsuitId, request.colors)
        print(similarList)
        return {"similarList": similarList}
    except Exception as e:
        print(e)
        raise HTTPException(status_code=500, detail=str(e))