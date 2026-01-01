import os
import uvicorn

if __name__ == "__main__":
    # 환경 변수에서 로그 레벨을 읽어옵니다 (기본값 info)
    log_level = os.getenv("LOG_LEVEL", "info").lower()

    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=False,
        log_level=log_level
    )
