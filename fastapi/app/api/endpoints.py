from fastapi import APIRouter, HTTPException, Depends, BackgroundTasks
from sqlalchemy import text
from sqlalchemy.orm import Session
from app.db import get_db
from app.schemas import CrawlRequest, CrawlResponse, SwimsuitRequest
from app.services.crawler_service import crawl_swimsuit_and_extract_colors, crawl_swimcap_and_extract_colors
from app.services.similarity_service import recommend_swim_caps
from app.enums import ItemType
import httpx

router = APIRouter()


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
async def crawl_swimsuit(request: CrawlRequest, background_tasks: BackgroundTasks):
    """수영복 크롤링 엔드포인트"""
    print(f"#### [요청 데이터 확인]: {request}")
    try:
        # 백그라운드에서 크롤링 시작
        background_tasks.add_task(
            run_crawling_task,
            request.logId,
            request.crawlingUrl,
            request.callbackUrl,
            ItemType.SWIMSUIT
        )

        return {"logId": request.logId}

    except Exception as e:
        import traceback
        print("#### [에러 발생 상세 로그] ####")
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/crawl/swimcaps")
async def crawl_swimcap(request: CrawlRequest, background_tasks: BackgroundTasks):
    """수모 크롤링 엔드포인트"""
    print(f"#### [요청 데이터 확인]: {request}")
    try:
        # 백그라운드에서 크롤링 시작
        background_tasks.add_task(
            run_crawling_task,
            request.logId,
            request.crawlingUrl,
            request.callbackUrl,
            ItemType.SWIMCAP
        )

        return {"logId": request.logId}

    except Exception as e:
        import traceback
        print("#### [에러 발생 상세 로그] ####")
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))


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
    async with httpx.AsyncClient() as client:
        try:
            # ItemType에 따라 적절한 크롤링 함수 호출
            if item_type == ItemType.SWIMSUIT:
                extracted_products = crawl_swimsuit_and_extract_colors(url)
            elif item_type == ItemType.SWIMCAP:
                extracted_products = crawl_swimcap_and_extract_colors(url)
            else:
                raise ValueError(f"Unknown item type: {item_type}")

            # 성공 응답 생성
            response_data = CrawlResponse(
                logId=log_id,
                crawlStatus="COMPLETED",
                errorMsg="",
                products=extracted_products
            )
        except Exception as e:
            # 실패 응답 생성
            response_data = CrawlResponse(
                logId=log_id,
                crawlStatus="FAILED",
                errorMsg=str(e),
                products=[]
            )

        # 콜백 URL로 결과 전송
        await client.post(callback_url, json=response_data.model_dump())
