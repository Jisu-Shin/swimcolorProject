from fastapi import FastAPI
from app.api.endpoints import router

app = FastAPI(
    title="Swimcolor API",
    description="수영복, 수모 색상 추출 API",
    version="1.0.0"
)

# API 라우터 등록
app.include_router(router)

