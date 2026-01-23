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

from app.config import settings
from app.extractors.yolo_utils import crop_swimsuit_only
from app.extractors.color_utils import extract_colors_kmeans

logger = logging.getLogger(__name__)


# ============================================================================
# 설정 클래스
# ============================================================================

class ExtractorConfig:
    """색상 추출기 설정값"""
    THUMBNAIL_SIZE = (160, 160)  # 이미지 썸네일 크기
    DOWNLOAD_WORKERS = 4  # 다운로드 스레드 수
    COMPUTE_WORKERS = 2  # 색상 추출 스레드 수 (CPU 코어 수)
    CONF_THRESHOLD = 0.5  # YOLO 신뢰도 임계값
    DEFAULT_N_COLORS = 5  # 기본 추출 색상 개수


# ============================================================================
# 헬퍼 함수
# ============================================================================

def _process_single_image(
        img: np.ndarray,
        model,
        n_colors: int,
        conf_threshold: float = ExtractorConfig.CONF_THRESHOLD
) -> List[Dict[str, Any]]:
    """
    단일 이미지 처리 (스레드에서 실행)
    
    Args:
        img: OpenCV 이미지 (BGR)
        model: YOLO 모델 인스턴스
        n_colors: 추출할 색상 개수
        conf_threshold: 탐지 신뢰도 임계값
        
    Returns:
        colors: 색상 정보 리스트 또는 빈 리스트 (실패 시)
    """
    try:
        # 1. 수영복/수모 영역 크롭
        cropped = crop_swimsuit_only(
            img,
            model,
            conf_threshold=conf_threshold
        )

        logger.debug("객체 크롭 완료")

        # 2. 색상 추출
        colors = extract_colors_kmeans(cropped, n_colors)

        logger.debug(f"색상 추출 완료: {len(colors)}개")

        # 3. 메모리 정리
        del cropped
        gc.collect()

        return colors

    except ValueError as e:
        logger.warning(f"객체 탐지 실패: {e}")
        return []
    except Exception as e:
        logger.error(f"이미지 처리 중 오류: {e}", exc_info=True)
        return []


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

    def __init__(self, yolo_model_path: str = settings.yolo_model_path):
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
        self.compute_executor = ThreadPoolExecutor(
            max_workers=ExtractorConfig.COMPUTE_WORKERS
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

            # 썸네일 생성 (메모리 절약)
            img.thumbnail(ExtractorConfig.THUMBNAIL_SIZE, Image.LANCZOS)

            return cv2.cvtColor(np.array(img), cv2.COLOR_RGB2BGR)
        else:
            # 로컬 파일
            img = cv2.imread(image_source)
            if img is None:
                raise ValueError(f"이미지를 읽을 수 없습니다: {image_source}")
            return img

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

        # 예외 필터링
        valid_images = []
        for i, img in enumerate(images):
            if isinstance(img, Exception):
                logger.warning(
                    f"이미지 로드 실패 [{i}]: {image_urls[i]} - {img}"
                )
            else:
                valid_images.append(img)

        logger.info(
            f"이미지 로드 완료: {len(valid_images)}/{len(image_urls)} 성공"
        )

        return valid_images

    async def extract_colors_parallel(
            self,
            images: List[np.ndarray],
            n_colors: int = ExtractorConfig.DEFAULT_N_COLORS,
            conf_threshold: float = ExtractorConfig.CONF_THRESHOLD
    ) -> List[List[Dict[str, Any]]]:
        """
        여러 이미지의 색상을 병렬로 추출
        
        Args:
            images: OpenCV 이미지 리스트
            n_colors: 추출할 색상 개수
            conf_threshold: YOLO 탐지 신뢰도 임계값
            
        Returns:
            all_colors: 각 이미지별 색상 리스트
        """
        if not images:
            return []

        loop = asyncio.get_running_loop()

        # 병렬 색상 추출
        tasks = [
            loop.run_in_executor(
                self.compute_executor,
                _process_single_image,
                img,
                self.model,
                n_colors,
                conf_threshold
            ) for img in images
        ]

        results = await asyncio.gather(*tasks)

        return results

    async def extract_segment_colors(
            self,
            image_urls: List[str],
            n_colors: int = ExtractorConfig.DEFAULT_N_COLORS,
            conf_threshold: float = ExtractorConfig.CONF_THRESHOLD,
    ) -> List[List[Dict[str, Any]]]:
        """
        전체 파이프라인: 이미지 다운로드 → 색상 추출
        
        Args:
            image_urls: 이미지 URL 리스트
            n_colors: 추출할 색상 개수
            conf_threshold: 탐지 신뢰도 임계값

        Returns:
            all_colors: 각 이미지별 색상 리스트
        """
        logger.debug("=" * 60)
        logger.debug("🏊 색상 추출 파이프라인 시작")
        logger.debug("=" * 60)

        if not image_urls:
            return []

        start_time = time.time()

        # 1. 이미지 다운로드
        logger.info("1️⃣ 이미지 병렬 다운로드 중...")
        images = await self.load_images_parallel(image_urls)

        if not images:
            logger.warning("다운로드된 이미지가 없습니다.")
            return []

        # 2. 색상 추출
        logger.info("2️⃣ 병렬 색상 추출 중...")
        all_colors = await self.extract_colors_parallel(
            images,
            n_colors,
            conf_threshold
        )

        # 3. 결과 요약
        total_time = time.time() - start_time
        success_count = sum(1 for colors in all_colors if colors)

        logger.info(f"🎉 전체 완료: {total_time:.1f}초")
        logger.info(f"   성공: {success_count}/{len(all_colors)}개")

        return all_colors

    def __del__(self):
        """리소스 정리"""
        try:
            self.download_executor.shutdown(wait=False)
            self.compute_executor.shutdown(wait=False)
        except:
            pass


# ============================================================================
# 사용 예시
# ============================================================================

async def main():
    """테스트용 메인 함수"""
    model_path = "../../" + settings.swimcap_yolo_model_path
    print(f"모델 경로: {model_path}")

    if not os.path.exists(model_path):
        print(f"❌ 모델 파일을 찾을 수 없습니다: {model_path}")
        return

    # 1. 색상 추출기 초기화
    extractor = ColorExtractorParallel(str(model_path))

    # 2. 테스트 이미지
    image_path = '/Users/zsu/MyProject/크롤링 사진/swimcap_1228/0024_피닉스_웨일드림 실.jpg'
    image_urls = [image_path]

    try:
        # 3. 색상 추출
        all_colors = await extractor.extract_segment_colors(
            image_urls=image_urls,
            n_colors=3,
            conf_threshold=0.5,
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
