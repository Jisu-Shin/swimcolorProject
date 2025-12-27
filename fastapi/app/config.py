from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # 모델 경로
    yolo_model_path: str = "ml/runs/segment/swimsuit-seg3/weights/best.pt"

    # 색상 추출 기본값
    default_n_colors: int = 3
    default_conf_threshold: float = 0.5

    # 크롤링 설정
    headless_browser: bool = True

    class Config:
        env_file = ".venv"


settings = Settings()