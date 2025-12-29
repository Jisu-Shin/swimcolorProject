import cv2
import numpy as np
from ultralytics import YOLO
from sklearn.cluster import KMeans
from collections import Counter
import requests
from io import BytesIO
from PIL import Image
import matplotlib.pyplot as plt
from app.config import settings


class ColorExtractor:
    """
    YOLO로 수영복 탐지 → 해당 영역만 크롭 → K-means로 색상 추출
    바운딩 박스 그리기가 아닌, 객체 추출 용도로 YOLO 사용
    """

    def __init__(self, yolo_model_path=settings.yolo_model_path):
        """YOLO 모델 초기화"""
        self.model = YOLO(yolo_model_path)
        print(f"✓ YOLO 모델 로드 완료: {yolo_model_path}")

    def load_image(self, image_source):
        """
        이미지 로드 (URL 또는 로컬 경로)

        Returns:
        - image: OpenCV BGR 이미지
        """
        if image_source.startswith('http'):
            # URL에서 다운로드
            response = requests.get(image_source)
            img = Image.open(BytesIO(response.content))
            return cv2.cvtColor(np.array(img), cv2.COLOR_RGB2BGR)
        else:
            # 로컬 파일
            return cv2.imread(image_source)

    def crop_swimsuit_only(self, image, conf_threshold=0.5, target_class=0):
        """
        ⭐ 핵심 기능: YOLO로 수영복 탐지 후 해당 영역만 크롭

        Parameters:
        - image: OpenCV 이미지 (BGR)
        - conf_threshold: 탐지 신뢰도 임계값
        - target_class: 탐지할 클래스 ID (커스텀 모델의 경우 swimsuit class)

        Returns:
        - cropped_image: 수영복 영역만 크롭된 이미지
        - detection_info: 탐지 정보 (bbox, confidence)
        """
        # YOLO 추론
        results = self.model(image, verbose=False)
        r = results[0]

        # 탐지된 결과에서 가장 신뢰도 높은 수영복 찾기
        best_detection = None
        max_confidence = 0

        for i, box in enumerate(r.boxes):
            conf = float(box.conf[0])
            cls = int(box.cls[0])

            # 조건: 타겟 클래스 && 신뢰도 임계값 이상 && 가장 높은 신뢰도
            if cls == target_class and conf >= conf_threshold and conf > max_confidence:
                best_detection = i
                max_confidence = conf

        if best_detection is None:
            raise ValueError(f"수영복을 탐지하지 못했습니다. (신뢰도 임계값: {conf_threshold})")

        # 수영복 영역만 크롭
        mask = r.masks.data[best_detection].cpu().numpy()  # (H, W), 0~1
        mask = (mask * 255).astype("uint8")
        print("수영복 영역만 mask를 통해 크롭 완료")

        # 크기 맞추기
        h, w = image.shape[:2]
        mask = cv2.resize(mask, (w, h))  # (width, height) 순서!
        # print(f"image shape: {image.shape}")  # (H, W, 3)
        # print(f"mask shape: {mask.shape}")  # (h, w) ← 다를 수 있음!
        print("크기 맞추기 완료")

        # 3채널 마스크
        mask_3c = cv2.merge([mask, mask, mask])
        print("3채널 마스크 완료")
        # print(f"mask_3c shape: {mask_3c.shape}")  # (h, w, 3)

        swimsuit_only = cv2.bitwise_and(image, mask_3c)
        return swimsuit_only

    def extract_colors_kmeans(self, image, n_colors=5, remove_extreme=True):
        """
        K-means 클러스터링으로 주요 색상 추출

        Parameters:
        - image: OpenCV 이미지 (BGR)
        - n_colors: 추출할 색상 개수
        - remove_extreme: 극단적인 밝기/어두움 제거 (배경 노이즈 제거)

        Returns:
        - colors: 색상 정보 리스트 [{rgb, hex, ratio}, ...]
        """
        # BGR → RGB 변환
        rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

        # 이미지를 1차원 픽셀 배열로 변환
        pixels = rgb_image.reshape(-1, 3)

        # 극단적인 색상 제거 (선택적)
        if remove_extreme:
            # 밝기 계산 (평균)
            brightness = np.mean(pixels, axis=1)

            # 너무 밝거나 어두운 픽셀 제거 (배경/그림자 제거)
            mask = (brightness > 25) & (brightness < 230)
            pixels_filtered = pixels[mask]

            if len(pixels_filtered) < 100:  # 필터링 후 픽셀이 너무 적으면
                pixels_filtered = pixels  # 원본 사용

            pixels = pixels_filtered

        # K-means 클러스터링
        n_clusters = min(n_colors, len(pixels))  # 픽셀보다 많은 클러스터 방지

        kmeans = KMeans(
            n_clusters=n_clusters,
            random_state=42,
            n_init=10,
            max_iter=300
        )
        kmeans.fit(pixels)

        # 각 클러스터(색상)의 픽셀 개수 세기
        labels = kmeans.labels_
        label_counts = Counter(labels)
        total_pixels = len(labels)

        # 색상 정보 생성
        colors = []
        for i, center in enumerate(kmeans.cluster_centers_):
            rgb = center.astype(int)
            count = label_counts[i]
            ratio = count / total_pixels

            colors.append({
                'rgb': rgb.tolist(),
                'hex': '#{:02x}{:02x}{:02x}'.format(*rgb),
                'ratio': ratio,
                'count': count
            })

        # 비율 순으로 정렬 (가장 많이 나타나는 색상 우선)
        colors.sort(key=lambda x: x['ratio'], reverse=True)

        print(f"✓ {len(colors)}개의 주요 색상 추출 완료")

        return colors

    def visualize_extraction(self, original_image, swimsuit_only, colors, mask=None, save_path=None):
        """
        mask 적용된 수영복 + 마스크 시각화
        """
        fig = plt.figure(figsize=(16, 8))  # 높이 늘림 (마스크 추가)

        # 2. 마스크 적용된 수영복 (기존 cropped_image 위치)
        ax2 = plt.subplot(2, 3, 2)
        ax2.imshow(cv2.cvtColor(swimsuit_only, cv2.COLOR_BGR2RGB))  # ✅ 그대로!
        ax2.set_title('Swimsuit Only (Mask Applied)', fontsize=12, fontweight='bold')
        ax2.axis('off')

        # 3️⃣ ⭐ 마스크 이미지 추가 (새로!)
        ax3 = plt.subplot(2, 3, 3)
        if mask is not None:
            # mask를 RGB로 변환해서 보여주기
            mask_rgb = cv2.cvtColor(mask, cv2.COLOR_GRAY2RGB)
            ax3.imshow(mask_rgb)
            ax3.set_title('Segmentation Mask', fontsize=12, fontweight='bold')
        ax3.axis('off')

        # 4. 색상 팔레트 (기존 그대로)
        ax4 = plt.subplot(2, 3, 4)
        n_colors = len(colors)
        color_blocks = np.zeros((100, n_colors * 100, 3), dtype=np.uint8)
        for i, color_info in enumerate(colors):
            color_blocks[:, i * 100:(i + 1) * 100] = color_info['rgb']
            mid_x = i * 100 + 50
            ax4.text(mid_x, 30, color_info['hex'], ha='center', va='center',
                     fontsize=10, fontweight='bold', color='white',
                     bbox=dict(boxstyle='round', facecolor='black', alpha=0.7))
            ax4.text(mid_x, 70, f"{color_info['ratio'] * 100:.1f}%", ha='center',
                     va='center', fontsize=9, color='white',
                     bbox=dict(boxstyle='round', facecolor='black', alpha=0.7))
        ax4.imshow(color_blocks)
        ax4.set_title('Extracted Colors', fontsize=12, fontweight='bold')
        ax4.axis('off')

        # 5. 오버레이 (원본 + 마스크 투명도 적용)
        ax5 = plt.subplot(2, 3, 5)
        if mask is not None:
            overlay = original_image.copy()
            overlay[mask > 127] = [0, 255, 0]  # 수영복 영역 녹색
            ax5.imshow(cv2.cvtColor(overlay, cv2.COLOR_BGR2RGB))
            ax5.set_title('Original + Mask Overlay', fontsize=12, fontweight='bold')
        ax5.axis('off')

        plt.tight_layout()

        if save_path:
            plt.savefig(save_path, dpi=150, bbox_inches='tight')
        plt.show()

    def process_swimsuit_image(self, image_source, n_colors=5,
                               conf_threshold=0.5, visualize=True):
        """
        전체 파이프라인: 이미지 → 수영복 크롭 → 색상 추출

        Parameters:
        - image_source: 이미지 경로 또는 URL
        - n_colors: 추출할 색상 개수
        - conf_threshold: 탐지 신뢰도 임계값
        - visualize: 결과 시각화 여부

        Returns:
        - cropped_image: 크롭된 수영복 이미지
        - colors: 추출된 색상 리스트
        """
        print("\n" + "=" * 60)
        print("🏊 수영복 색상 추출 시작")
        print("=" * 60 + "\n")

        # 1. 이미지 로드
        print("1️⃣ 이미지 로드 중...")
        original_image = self.load_image(image_source)
        print(f"   이미지 크기: {original_image.shape[1]}x{original_image.shape[0]}px\n")

        # 2. YOLO로 수영복 탐지 & 크롭
        print("2️⃣ YOLO로 수영복 탐지 중...")
        cropped_image = self.crop_swimsuit_only(
            original_image,
            conf_threshold=conf_threshold
        )
        print()

        # 3. K-means로 색상 추출
        print("3️⃣ K-means로 주요 색상 추출 중...")
        colors = self.extract_colors_kmeans(cropped_image, n_colors=n_colors)
        print()

        # 4. 결과 출력
        print("📊 추출된 색상 정보:")
        print("-" * 50)
        for i, color in enumerate(colors, 1):
            print(f"{i}. RGB{tuple(color['rgb'])} | {color['hex']} | {color['ratio'] * 100:.1f}%")
        print()

        # 5. 시각화
        if visualize:
            print("4️⃣ 결과 시각화 중...")
            self.visualize_extraction(
                original_image,
                cropped_image,
                colors
            )

        print("=" * 60)
        print("✅ 색상 추출 완료!")
        print("=" * 60 + "\n")

        return colors


# ============================================================
# 사용 예시
# ============================================================

if __name__ == '__main__':

    # 1. 색상 추출기 초기화
    extractor = ColorExtractor()
    # extractor = SwimwearColorExtractor('yolov8n.pt')
    # 커스텀 모델 사용 시: SwimwearColorExtractor('runs/detect/train/weights/best.pt')

    # 2. 이미지 처리 (URL 또는 로컬 경로)
    image_path = '/Users/zsu/MyProject/training_set/swimsuit_25_데이지 테이.jpg'  # 또는 'https://example.com/image.jpg'

    try:
        # 수영복 크롭 & 색상 추출
        cropped_swimsuit, colors = extractor.process_swimsuit_image(
            image_source=image_path,
            n_colors=5,  # 상위 5개 색상
            conf_threshold=0.5,  # 탐지 임계값 (낮추면 더 많이 탐지)
            visualize=True  # 결과 시각화
        )

        # # 3. 수모 추천
        # print("\n🎯 수모 추천 시스템")
        # print("=" * 60 + "\n")
        #
        # # 수모 데이터베이스 (예시)
        # cap_database = [
        #     {'id': 1, 'name': '스피도 실리콘 캡 (블루)', 'color': [30, 144, 255], 'price': 15000},
        #     {'id': 2, 'name': '아레나 프로 캡 (레드)', 'color': [220, 20, 60], 'price': 18000},
        #     {'id': 3, 'name': '나이키 스윔 캡 (블랙)', 'color': [30, 30, 30], 'price': 12000},
        #     {'id': 4, 'name': '미즈노 라텍스 캡 (옐로우)', 'color': [255, 215, 0], 'price': 10000},
        #     {'id': 5, 'name': '아디다스 스윔 캡 (그린)', 'color': [50, 205, 50], 'price': 13000},
        #     {'id': 6, 'name': '티어 실리콘 캡 (핑크)', 'color': [255, 105, 180], 'price': 16000},
        #     {'id': 7, 'name': '조그스 프로 캡 (퍼플)', 'color': [138, 43, 226], 'price': 17000},
        #     {'id': 8, 'name': '펀키 트런스 캡 (오렌지)', 'color': [255, 140, 0], 'price': 14000},
        # ]
        #
        # # 추천 실행
        # recommendations = recommend_swim_caps(colors, cap_database, top_n=3)
        #
        # print("💡 추천 결과 (상위 3개):")
        # print("-" * 60)
        # for i, rec in enumerate(recommendations, 1):
        #     print(f"{i}. {rec['cap_name']}")
        #     print(
        #         f"   색상: {rec['cap_color_hex']} ← 수영복 {rec['matched_swimsuit_color']} ({rec['matched_color_ratio'] * 100:.1f}%)와 매칭")
        #     print(f"   가격: {rec['price']}원")
        #     print(f"   유사도: {rec['similarity_score']:.2f} (낮을수록 유사)\n")

    except ValueError as e:
        print(f"❌ 오류: {e}")
        print("   → 신뢰도 임계값(conf_threshold)을 낮춰보세요.")
    except Exception as e:
        print(f"❌ 예상치 못한 오류: {e}")