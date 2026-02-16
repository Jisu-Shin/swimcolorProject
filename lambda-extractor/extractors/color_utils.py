"""
색상 추출 유틸리티 함수

K-means 클러스터링 기반 주요 색상 추출
"""

import cv2
import numpy as np
from typing import List, Dict, Any
from sklearn.cluster import KMeans
from collections import Counter
import logging

logger = logging.getLogger(__name__)


class ColorExtractionConfig:
    """색상 추출 설정값"""
    DEFAULT_N_COLORS = 5
    SATURATION_THRESHOLD = 20  # HSV 채도 임계값
    BRIGHTNESS_MIN = 15  # 최소 밝기
    BRIGHTNESS_MAX = 240  # 최대 밝기
    MIN_PIXELS = 100  # 최소 픽셀 수
    KMEANS_N_INIT = 5  # K-means 초기화 횟수
    KMEANS_MAX_ITER = 50  # K-means 최대 반복


def extract_colors_kmeans(
        image: np.ndarray,
        n_colors: int = ColorExtractionConfig.DEFAULT_N_COLORS,
        remove_extreme: bool = True
) -> List[Dict[str, Any]]:
    """
    K-means 클러스터링으로 주요 색상 추출
    
    Args:
        image: OpenCV 이미지 (BGR)
        n_colors: 추출할 색상 개수
        remove_extreme: 극단적인 밝기/어두움 제거 여부
        
    Returns:
        colors: 색상 정보 리스트
            [{
                'rgb': [R, G, B],
                'hex': '#RRGGBB',
                'ratio': 0.0~1.0,
                'count': int
            }, ...]
    """
    # BGR → RGB 변환
    rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

    # HSV로 변환하여 채도 필터링 (회색/무채색 제거)
    hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
    sat_mask = hsv[:, :, 1] > ColorExtractionConfig.SATURATION_THRESHOLD

    # 이미지를 1차원 픽셀 배열로 변환
    rgb_pixels = rgb_image.reshape(-1, 3)
    pixels = rgb_pixels[sat_mask.flatten()]

    # 필터링된 픽셀이 너무 적으면 원본 사용
    if len(pixels) < ColorExtractionConfig.MIN_PIXELS:
        logger.warning(
            f"필터링된 픽셀 수 부족 ({len(pixels)}), 원본 사용"
        )
        pixels = rgb_pixels

    # 극단적인 색상 제거 (배경/그림자 제거)
    if remove_extreme:
        pixels = _filter_extreme_brightness(pixels)

    # K-means 클러스터링
    colors = _perform_kmeans(pixels, n_colors)

    logger.info(f"✓ {len(colors)}개의 주요 색상 추출 완료")

    return colors


def _filter_extreme_brightness(pixels: np.ndarray) -> np.ndarray:
    """
    극단적인 밝기의 픽셀 제거
    
    Args:
        pixels: RGB 픽셀 배열 (N, 3)
        
    Returns:
        filtered_pixels: 필터링된 픽셀 배열
    """
    # 밝기 계산 (RGB 평균)
    brightness = np.mean(pixels, axis=1)

    # 적절한 범위의 픽셀만 선택
    mask = (
            (brightness > ColorExtractionConfig.BRIGHTNESS_MIN) &
            (brightness < ColorExtractionConfig.BRIGHTNESS_MAX)
    )

    filtered = pixels[mask]

    # 필터링 후 픽셀이 너무 적으면 원본 반환
    if len(filtered) < ColorExtractionConfig.MIN_PIXELS:
        logger.debug("밝기 필터링 후 픽셀 부족, 원본 사용")
        return pixels

    logger.debug(
        f"밝기 필터링: {len(pixels)} → {len(filtered)} 픽셀"
    )

    return filtered


def _perform_kmeans(
        pixels: np.ndarray,
        n_colors: int
) -> List[Dict[str, Any]]:
    """
    K-means 클러스터링 수행
    
    Args:
        pixels: RGB 픽셀 배열 (N, 3)
        n_colors: 클러스터 개수
        
    Returns:
        colors: 색상 정보 리스트
    """
    # 픽셀 수보다 많은 클러스터 방지
    n_clusters = min(n_colors, len(pixels))

    # K-means 클러스터링
    kmeans = KMeans(
        n_clusters=n_clusters,
        random_state=42,
        n_init=ColorExtractionConfig.KMEANS_N_INIT,
        max_iter=ColorExtractionConfig.KMEANS_MAX_ITER
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
            'hex': rgb_to_hex(rgb),
            'ratio': ratio,
            'count': count
        })

    # 비율 순으로 정렬 (많이 나타나는 색상 우선)
    colors.sort(key=lambda x: x['ratio'], reverse=True)

    return colors


def rgb_to_hex(rgb: np.ndarray) -> str:
    """
    RGB 값을 HEX 색상 코드로 변환
    
    Args:
        rgb: RGB 배열 [R, G, B]
        
    Returns:
        hex_color: HEX 색상 코드 (예: '#FF5733')
    """
    return '#{:02x}{:02x}{:02x}'.format(*rgb)


def hex_to_rgb(hex_color: str) -> List[int]:
    """
    HEX 색상 코드를 RGB 값으로 변환
    
    Args:
        hex_color: HEX 색상 코드 (예: '#FF5733' 또는 'FF5733')
        
    Returns:
        rgb: RGB 배열 [R, G, B]
    """
    hex_color = hex_color.lstrip('#')
    return [int(hex_color[i:i + 2], 16) for i in (0, 2, 4)]


def filter_similar_colors(
        colors: List[Dict[str, Any]],
        threshold: float = 30.0
) -> List[Dict[str, Any]]:
    """
    유사한 색상 제거 (유클리드 거리 기반)
    
    Args:
        colors: 색상 리스트
        threshold: 유사도 임계값 (낮을수록 엄격)
        
    Returns:
        filtered_colors: 중복 제거된 색상 리스트
    """
    if not colors:
        return []

    filtered = [colors[0]]

    for color in colors[1:]:
        rgb = np.array(color['rgb'])
        is_similar = False

        for existing in filtered:
            existing_rgb = np.array(existing['rgb'])
            distance = np.linalg.norm(rgb - existing_rgb)

            if distance < threshold:
                is_similar = True
                break

        if not is_similar:
            filtered.append(color)

    logger.debug(
        f"유사 색상 필터링: {len(colors)} → {len(filtered)} 색상"
    )

    return filtered
