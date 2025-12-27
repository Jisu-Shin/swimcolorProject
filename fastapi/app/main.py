# from fastapi import Depends
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session
from .services.crawler_service import crawl_and_extract_colors
# from .database import get_db

class CrawlRequest(BaseModel):
    url: str

class CrawlResponse(BaseModel):
    products: list

app = FastAPI(
    title="Swimcolor API",
    description="수영복, 수모 색상 추출 API",
    version="1.0.0"
)

# @app.get("/")
# async def root(db: Session = Depends(get_db)):  # db 추가
#     # DB 연결 테스트 (선택적)
#     try:
#         # 간단한 쿼리로 연결 확인
#         result = db.execute("SELECT 1").scalar()
#         db_status = "OK" if result else "Error"
#     except:
#         db_status = "Error"
#
#     return {
#         "message": "Swimcolor API",
#         "version": "1.0.0",
#         "docs": "/docs",
#         "db_status": db_status  # 연결 상태 확인
#     }

@app.get("/")
async def root():
    return {
        "message": "Swimcolor API",
        "version": "1.0.0",
        "docs": "/docs"
    }

@app.get("/health")
async def health_check():
    return {"status": "healthy"}

# 크롤링
@app.post("/crawl")
async def crawl(request: CrawlRequest):
    # print(urlInfo.url)
    try :
        products = crawl_and_extract_colors(request.url)
        return {"products": products}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))