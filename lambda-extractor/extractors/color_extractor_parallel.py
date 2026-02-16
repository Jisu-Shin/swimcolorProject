"""
병렬 색상 추출 서비스

YOLO 세그멘테이션 + K-means 색상 추출을 병렬로 처리
ThreadPoolExecutor 사용으로 메모리 효율적인 병렬 처리
"""

import cv2
import numpy as np
from ultralytics import YOLO
from concurrent.futures import ThreadPoolExecutor
from typing import List, Dict, Any
import requests
from io import BytesIO
from PIL import Image
import asyncio
import gc
import time
import logging
import os

from yolo_utils import apply_mask
from color_utils import extract_colors_kmeans

logger = logging.getLogger(__name__)

# ============================================================================
# 설정 클래스
# ============================================================================

class ExtractorConfig:
    """색상 추출기 설정값"""
    THUMBNAIL_SIZE = (416, 416)  # 이미지 썸네일 크기 32배수
    DOWNLOAD_WORKERS = 4  # 다운로드 스레드 수
    CONF_THRESHOLD = 0.5  # YOLO 신뢰도 임계값
    DEFAULT_N_COLORS = 3  # 기본 추출 색상 개수
    BATCH_SIZE = 3


# ============================================================================
# 메인 클래스
# ============================================================================

class ColorExtractorParallel:
    """
    병렬 색상 추출기
    
    YOLO 세그멘테이션으로 수영복/수모를 탐지하고
    K-means 클러스터링으로 주요 색상을 추출합니다.
    
    ThreadPoolExecutor를 사용하여 메모리 효율적인 병렬 처리를 수행합니다.
    """

    def __init__(self, yolo_model_path: str):
        """
        색상 추출기 초기화
        
        Args:
            yolo_model_path: YOLO 모델 파일 경로
        """
        self.model_path = yolo_model_path

        # YOLO 모델 로드 (메인 스레드에서 1회)
        logger.info(f"YOLO 모델 로드 중: {yolo_model_path}")
        self.model = YOLO(yolo_model_path)
        logger.info("✓ YOLO 모델 로드 완료")

        # Executor 생성
        self.download_executor = ThreadPoolExecutor(
            max_workers=ExtractorConfig.DOWNLOAD_WORKERS
        )

    def load_image(self, image_source: str) -> np.ndarray:
        """
        이미지 로드 (URL 또는 로컬 경로)
        
        Args:
            image_source: 이미지 URL 또는 로컬 파일 경로
            
        Returns:
            image: OpenCV BGR 이미지
        """
        if image_source.startswith('http'):
            # URL에서 다운로드
            response = requests.get(image_source, timeout=10)
            img = Image.open(BytesIO(response.content))
            img_array = cv2.cvtColor(np.array(img), cv2.COLOR_RGB2BGR)

        else:
            # 로컬 파일
            img_array = cv2.imread(image_source)
            if img_array is None:
                raise ValueError(f"이미지를 읽을 수 없습니다: {image_source}")

        target_size = ExtractorConfig.THUMBNAIL_SIZE[0]

        # 비율 유지하며 리사이즈 + 패딩
        h, w = img_array.shape[:2]
        scale = min(target_size / h, target_size / w)
        new_h, new_w = int(h * scale), int(w * scale)

        resized = cv2.resize(img_array, (new_w, new_h), interpolation=cv2.INTER_LINEAR)
        del img_array  # 원본 이미지 해제

        # 중앙 배치 + 검은색 패딩
        canvas = np.zeros((target_size, target_size, 3), dtype=np.uint8)
        y_offset = (target_size - new_h) // 2
        x_offset = (target_size - new_w) // 2
        canvas[y_offset:y_offset + new_h, x_offset:x_offset + new_w] = resized
        del resized  # 리사이즈된 이미지 해제

        return canvas

    async def load_images_parallel(
            self,
            image_urls: List[str]
    ) -> List[np.ndarray]:
        """
        여러 이미지 병렬 다운로드 & 로드
        
        Args:
            image_urls: 이미지 URL 리스트
            
        Returns:
            images: OpenCV 이미지 리스트
        """
        if not image_urls:
            return []

        loop = asyncio.get_running_loop()

        # 병렬 다운로드
        images = await asyncio.gather(
            *[
                loop.run_in_executor(
                    self.download_executor,
                    self.load_image,
                    url
                ) for url in image_urls
            ],
            return_exceptions=True
        )

        logger.info(
            f"이미지 로드 완료: {len(image_urls)} 성공"
        )

        return images

    def process_batch(
            self,
            images: List[np.ndarray]
    ) -> List[List[Dict[str, Any]]]:
        """
        이미지 배치 처리 (YOLO 배치 추론)

        Args:
            images: 이미지 배치 (최대 4개 권장)
            n_colors: 추출할 색상 개수
            conf_threshold: 탐지 신뢰도

        Returns:
            각 이미지별 색상 리스트
        """
        all_colors = []

        try:
            # 1. YOLO 배치 추론 (핵심!)
            results = self.model(
                images,
                verbose=False,
                conf=ExtractorConfig.CONF_THRESHOLD,
                imgsz=ExtractorConfig.THUMBNAIL_SIZE[0]
            )

            # 2. 각 이미지별로 처리
            for i, (img, result) in enumerate(zip(images, results)):
                try:
                    # 마스크 적용
                    cropped = apply_mask(img, result, ExtractorConfig.CONF_THRESHOLD)

                    # 색상 추출
                    colors = extract_colors_kmeans(cropped, ExtractorConfig.DEFAULT_N_COLORS)

                    all_colors.append(colors)

                    del cropped

                except Exception as e:
                    logger.warning(f"배치 {i}번 이미지 처리 실패: {e}")
                    all_colors.append([])

            # 배치 처리 후 즉시 정리
            del results
            gc.collect()

        except Exception as e:
            logger.error(f"*** 배치 묶음 처리 실패: {e}", exc_info=True)
            all_colors = [[] for _ in images]

        return all_colors

    async def extract_colors(
            self,
            image_urls: List[str]
    ) -> List[List[Dict[str, Any]]]:
        """
        전체 파이프라인: 이미지 다운로드 → 색상 추출
        
        Args:
            image_urls: 이미지 URL 리스트

        Returns:
            all_colors: 각 이미지별 색상 리스트
        """
        logger.debug("=" * 60)
        logger.debug("🏊 색상 추출 파이프라인 시작")
        logger.debug("=" * 60)

        if not image_urls:
            return []

        # 1. 이미지 다운로드
        logger.info("1️⃣ 이미지 병렬 다운로드 중...")
        start_time = time.time()
        images = await self.load_images_parallel(image_urls)
        load_image_complete_time = time.time()
        load_image_time = load_image_complete_time - start_time
        logger.info(f"이미지 다운로드 소요시간 : {load_image_time:.3f}s")

        if not images:
            logger.warning("다운로드된 이미지가 없습니다.")
            return []

        # 2. 색상 추출
        logger.info("2️⃣ 이미지 배치 처리 중...")
        all_colors = []
        batch_size = ExtractorConfig.BATCH_SIZE

        for i in range(0, len(images), batch_size):
            batch = images[i:i + batch_size]
            batch_num = i // batch_size + 1
            total_batches = (len(images) + batch_size - 1) // batch_size

            logger.info(f"   배치 {batch_num}/{total_batches} 처리 중...")

            # 배치 처리
            batch_colors = self.process_batch(batch)
            all_colors.extend(batch_colors)

        color_time = time.time() - load_image_complete_time
        logger.info(f"병렬 색상 추출 완료: {color_time:.3f}s")

        # 3. 결과 요약
        total_time = time.time() - start_time
        success_count = sum(1 for colors in all_colors if colors)

        # logger.info(f"🎉 전체 완료: {total_time:.1f}초")
        logger.info(f"   성공: {success_count}/{len(all_colors)}개")

        return all_colors

    def __del__(self):
        """리소스 정리"""
        try:
            self.download_executor.shutdown(wait=False)
        except:
            pass


# ============================================================================
# 사용 예시
# ============================================================================

async def main():
    """테스트용 메인 함수"""
    model_path = "../../"
    print(f"모델 경로: {model_path}")

    if not os.path.exists(model_path):
        print(f"❌ 모델 파일을 찾을 수 없습니다: {model_path}")
        return

    # 1. 색상 추출기 초기화
    extractor = ColorExtractorParallel(str(model_path))

    # 2. 테스트 이미지
    image_path = '/Users/zsu/MyProject/크롤링 사진/swimsuit_0206/0049_움파_시그니처 싱.jpg'
    # image_path2 = '/Users/zsu/MyProject/크롤링 사진/swimsuit_0206/0050_움파_아가일 싱글.jpg'
    # image_path3 = '/Users/zsu/MyProject/크롤링 사진/swimsuit_0206/0051_움파_블랙펄 더블.jpg'
    # image_path4 = '/Users/zsu/MyProject/크롤링 사진/swimsuit_0206_02/0071_스웨이브_세레니티 크.jpg'
    # image_urls = [image_path,image_path2, image_path3, image_path4]
    image_urls = [image_path]

    try:
        # 3. 색상 추출
        all_colors = await extractor.extract_colors(
            image_urls=image_urls
        )

        # 4. 결과 출력
        for i, colors in enumerate(all_colors, 1):
            print(f"\n이미지 {i}:")
            for j, color in enumerate(colors, 1):
                print(
                    f"  {j}. {color['hex']} | "
                    f"RGB{tuple(color['rgb'])} | "
                    f"{color['ratio'] * 100:.1f}%"
                )

    except Exception as e:
        print(f"❌ 오류: {e}")
        import traceback
        traceback.print_exc()


if __name__ == '__main__':
    asyncio.run(main())
