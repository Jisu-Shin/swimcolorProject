import os
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from dotenv import load_dotenv

# .env 파일 로드
load_dotenv()

# 환경 변수에서 DATABASE_URL 직접 가져오기 (우선순위 1)
DATABASE_URL = os.getenv("DATABASE_URL")

# DATABASE_URL이 없으면 개별 환경 변수로 구성 (우선순위 2)
if not DATABASE_URL:
    DB_USER = os.getenv("DB_USER")
    DB_PASSWORD = os.getenv("DB_PASSWORD")
    DB_HOST = os.getenv("DB_HOST", "localhost")
    DB_PORT = os.getenv("DB_PORT", "3306")
    DB_DATABASE = os.getenv("DB_DATABASE")
    
    if not all([DB_USER, DB_PASSWORD, DB_DATABASE]):
        raise Exception(
            "환경 변수가 설정되지 않았습니다. "
            ".env 파일에 DATABASE_URL 또는 DB_USER, DB_PASSWORD, DB_DATABASE를 설정하세요."
        )
    
    DATABASE_URL = f"mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_DATABASE}?charset=utf8mb4"

# SQLAlchemy 엔진 생성
engine = create_engine(
    DATABASE_URL,
    pool_size=3,            # 기본 연결 풀 크기
    max_overflow=5,         # 피크 시 추가 연결 수
    pool_pre_ping=True,     # 연결 유효성 사전 검사
    pool_recycle=3600,      # 1시간마다 연결 재사용 (MySQL wait_timeout 고려)
    echo=False              # 쿼리 로그 (개발: True, 프로덕션: False)
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


def get_db():
    """
    데이터베이스 세션을 생성하고 제공하는 의존성 함수
    FastAPI의 Depends와 함께 사용
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

