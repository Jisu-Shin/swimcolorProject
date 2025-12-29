import json
import os
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
SECRET_FILE = os.path.join(BASE_DIR, 'secrets.json')

# secrets.json 파일 안전하게 읽기
with open(SECRET_FILE, 'r', encoding='utf-8') as f:
    secrets = json.load(f)

DB = secrets["DB"]

# MySQL 데이터베이스 URL 생성
DATABASE_URL = f"mysql+pymysql://{DB['user']}:{DB['password']}@{DB['host']}:{DB['port']}/{DB['database']}?charset=utf8mb4"

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

# 세션 의존성 제공 함수
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
