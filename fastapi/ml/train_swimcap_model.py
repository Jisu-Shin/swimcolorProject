from ultralytics import YOLO


def main():
    """수영복 seg 모델 학습 실행"""
    print("🚀 수영복 YOLOv8 Segmentation 모델 학습 시작!")

    # 1. Roboflow에서 받은 데이터셋 압축해제 (이미 한 상태)
    model = YOLO("yolov8n-seg.pt")

    # 2. 바로 학습 시작
    results = model.train(
        data="/Users/zsu/MyProject/roboflow/roboflow_swimcap_1228/data.yaml",
        epochs=30,
        imgsz=640,
        batch=4,
        name="swimcap-seg",
        device='cpu',
        project="runs/segment",  # 결과 저장 폴더
        save=True,  # 가중치 저장
        plots=True,  # 학습 그래프 생성
    )

    print("✅ 학습 완료! 결과:", results)
    print("📁 모델 파일: runs/segment/swimcap-seg/weights/best.pt")


if __name__ == "__main__":
    main()