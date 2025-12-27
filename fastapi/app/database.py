# from sqlalchemy import create_engine
# from sqlalchemy.ext.declarative import declarative_base
# from sqlalchemy.orm import sessionmaker
#
# # Spring URL 변환: jdbc:h2:tcp://localhost/~/swimcolorTestDB → h2_tcp://
# DATABASE_URL = "h2://localhost:8082/~/swimcolorTestDB"
#
# engine = create_engine(
#     DATABASE_URL,
#     pool_size=3,            # 작게 유지
#     max_overflow=5,         # 피크시 추가
#     pool_pre_ping=True,     # 연결 유효성 검사
#     pool_recycle=180,       # 풀 3분 재사용 (Spring 기본값 참고)
#     echo=True               # 쿼리 로그 (개발용) -> 프로덕션에서 로그 OFF
# )
#
# SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
# Base = declarative_base()
#
# # 세션 의존성
# def get_db():
#     db = SessionLocal()
#     try:
#         yield db
#     finally:
#         db.close()
