from fastapi import FastAPI, HTTPException, Depends, BackgroundTasks
from sqlalchemy import text
from sqlalchemy.orm import Session
from app.db import get_db
from app.schemas import CrawlRequest, CrawlResponse, SwimsuitRequest
from app.services.crawler_service import crawl_swimsuit_and_extract_colors, crawl_swimcap_and_extract_colors
from app.services.similarity_service import recommend_swim_caps
import httpx

app = FastAPI(
    title="Swimcolor API",
    description="수영복, 수모 색상 추출 API",
    version="1.0.0"
)

@app.get("/")
async def root(db: Session = Depends(get_db)):  # db 추가
    # DB 연결 테스트 (선택적)
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
        "db_status": db_status  # 연결 상태 확인
    }

# @app.get("/")
# async def root():
#     return {
#         "message": "Swimcolor API",
#         "version": "1.0.0",
#         "docs": "/docs"
#     }

@app.get("/health")
async def health_check():
    return {"status": "healthy"}

# 수영복 크롤링
@app.post("/crawl/swimsuits")
async def crawl_swimsuit(request: CrawlRequest, background_tasks: BackgroundTasks):
    print(f"#### [요청 데이터 확인]: {request}")
    try:
        # 백그라운드에서 크롤링 시작 (Spring은 기다리지 않고 바로 202 응답을 받음)
        background_tasks.add_task(
            run_crawling_task,
            request.logId,
            request.crawlingUrl,
            request.callbackUrl
        )

        return {"logId": request.logId}

    except Exception as e:
        # 3. 에러 내용을 아주 상세하게 찍기
        import traceback
        print("#### [에러 발생 상세 로그] ####")
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))


async def run_crawling_task(log_id: int, url: str, callback_url: str):
    async with httpx.AsyncClient() as client:
        try:
            # 1. 크롤링 수행
            extracted_products = crawl_swimsuit_and_extract_colors(url)

            # 2. 클래스 객체 생성 (네가 만든 CrawlResponse 사용!)
            response_data = CrawlResponse(
                logId=log_id,
                crawlStatus="COMPLETED",
                errorMsg="",
                products=extracted_products
            )
        except Exception as e:
            # 실패 시에도 클래스 객체로 생성
            response_data = CrawlResponse(
                logId=log_id,
                crawlStatus="FAILED",
                errorMsg=str(e),
                products=[]
            )

        # 3. 전송 (response_data.model_dump()를 써서 JSON으로 변환)
        await client.post(callback_url, json=response_data.model_dump())

# 수모 크롤링
@app.post("/crawl/swimcaps")
async def crawl_swimcap(request: CrawlRequest):
    # print(urlInfo.url)
    try :
        products = crawl_swimcap_and_extract_colors(request.url)
        return {"products": products}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 수모 추천
@app.post("/recommend")
async def recommend_swimcaps(request: SwimsuitRequest, db: Session = Depends(get_db)):
    try :
        similarList = recommend_swim_caps(db, request.swimsuitId, request.colors)
        print(similarList)
        return {"similarList": similarList}
    except Exception as e:
        print(e)
        raise HTTPException(status_code=500, detail=str(e))
