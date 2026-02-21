from ultralytics import YOLO
import onnxruntime as ort
import numpy as np
import os
from app.config import settings


def convert_to_onnx(pt_path: str):
    """PyTorch 모델을 ONNX로 변환"""
    print(f"🔄 변환 시작: {pt_path}")

    # 1. PyTorch 모델 로드
    model = YOLO(pt_path)

    # 2. 모델 정보 출력
    print(f"📊 모델 정보:")
    print(f"   - 클래스 수: {len(model.names)}")
    print(f"   - 클래스: {model.names}")

    # 3. ONNX로 변환
    onnx_path = model.export(
        format='onnx',
        simplify=True,  # 그래프 최적화
        dynamic=True,  # 고정 배치 크기
        opset=17
    )

    # 4. 파일 크기 비교
    pt_size = os.path.getsize(pt_path) / 1024 / 1024
    onnx_size = os.path.getsize(onnx_path) / 1024 / 1024
    print(f"\n✅ 변환 완료!")
    print(f"   - 원본 (.pt): {pt_size:.2f}MB")
    print(f"   - 변환 (.onnx): {onnx_size:.2f}MB")

    # 5. 검증
    validate_onnx(onnx_path)

    return onnx_path


def validate_onnx(onnx_path: str):
    """ONNX 모델 정상 작동 확인"""
    print(f"\n🔍 ONNX 모델 검증 중...")

    # 세션 생성
    session = ort.InferenceSession(onnx_path)

    # 입력/출력 정보
    input_name = session.get_inputs()[0].name
    input_shape = session.get_inputs()[0].shape
    print(f"   입력: {input_name}, shape={input_shape}")

    # 더미 데이터로 추론 테스트
    dummy_input = np.random.randn(1, 3, 640, 640).astype(np.float32)
    outputs = session.run(None, {input_name: dummy_input})
    print(f"   ✅ 추론 테스트 성공!")
    print(f"   출력 shape: {[out.shape for out in outputs]}")


if __name__ == "__main__":
    model_path = "../ml/runs/segment/swimcolor-seg/weights/best.pt"
    print(f"모델 경로: {model_path}")
    convert_to_onnx(model_path)