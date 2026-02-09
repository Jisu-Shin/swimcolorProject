from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # 모델 경로
    yolo_model_path: str = "ml/runs/segment/swimsuit-seg2/weights/best.pt"
    swimsuit_onnx_path: str = "ml/runs/segment/swimsuit-seg2/weights/best.onnx"
    swimcap_yolo_model_path: str = "ml/runs/segment/swimcap-seg/weights/best.pt"
    swimcap_onnx_path: str = "ml/runs/segment/swimcap-seg/weights/best.onnx"

    # 크롤링 설정
    headless_browser: bool = True

    class Config:
        env_file = ".venv"


settings = Settings()