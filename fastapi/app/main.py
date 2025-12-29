from fastapi import FastAPI, HTTPException, Depends
from pydantic import BaseModel
from sqlalchemy import text
from sqlalchemy.orm import Session
from app.services.crawler_service import crawl_swimsuit_and_extract_colors, crawl_swimcap_and_extract_colors
from app.db import get_db

class CrawlRequest(BaseModel):
    url: str

class CrawlResponse(BaseModel):
    products: list

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

# 수영복크롤링
@app.post("/crawl/swimsuits")
async def crawl_swimsuit(request: CrawlRequest):
    # print(urlInfo.url)
    try :
        products = crawl_swimsuit_and_extract_colors(request.url)
        return {"products": products}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 크롤링
@app.post("/crawl/swimcaps")
async def crawl_swimcap(request: CrawlRequest):
    # print(urlInfo.url)
    try :
        products = crawl_swimcap_and_extract_colors(request.url)
        return {"products": products}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))