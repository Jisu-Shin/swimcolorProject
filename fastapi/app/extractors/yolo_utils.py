"""
YOLO 관련 유틸리티 함수

수영복/수모 탐지 및 세그멘테이션 처리
"""

import cv2
import numpy as np
from typing import Tuple, Optional
import logging

logger = logging.getLogger(__name__)


def crop_swimsuit_only(
        image: np.ndarray,
        model,
        conf_threshold: float = 0.5,
        target_class: int = 0
) -> np.ndarray:
    """
    YOLO로 수영복/수모 탐지 후 해당 영역만 크롭
    
    Args:
        image: OpenCV 이미지 (BGR)
        model: YOLO 모델 인스턴스
        conf_threshold: 탐지 신뢰도 임계값 (0.0 ~ 1.0)
        target_class: 탐지할 클래스 ID (기본: 0)
        
    Returns:
        cropped_image: 수영복/수모 영역만 크롭된 이미지
        
    Raises:
        ValueError: 객체를 탐지하지 못한 경우
    """
    # YOLO 추론
    results = model(image, verbose=False)
    r = results[0]

    # 탐지된 결과에서 가장 신뢰도 높은 객체 찾기
    best_detection = None
    max_confidence = 0.0

    for i, box in enumerate(r.boxes):
        conf = float(box.conf[0])
        cls = int(box.cls[0])

        # 조건: 타겟 클래스 && 신뢰도 임계값 이상 && 가장 높은 신뢰도
        if cls == target_class and conf >= conf_threshold and conf > max_confidence:
            best_detection = i
            max_confidence = conf

    if best_detection is None:
        raise ValueError(
            f"객체를 탐지하지 못했습니다. "
            f"(신뢰도 임계값: {conf_threshold})"
        )

    # 세그멘테이션 마스크 추출
    mask = r.masks.data[best_detection].cpu().numpy()  # (H, W), 0~1
    mask = (mask * 255).astype("uint8")

    # 이미지 크기에 맞춰 마스크 리사이즈
    h, w = image.shape[:2]
    mask = cv2.resize(mask, (w, h))  # (width, height) 순서

    # 3채널 마스크로 변환
    mask_3c = cv2.merge([mask, mask, mask])

    # 마스크 적용하여 객체만 추출
    cropped = cv2.bitwise_and(image, mask_3c)

    logger.debug(
        f"객체 탐지 성공: 신뢰도 {max_confidence:.2f}, "
        f"마스크 크기 {mask.shape}"
    )

    return cropped


def get_segmentation_mask(
        image: np.ndarray,
        model,
        conf_threshold: float = 0.5,
        target_class: int = 0
) -> Optional[np.ndarray]:
    """
    YOLO로 세그멘테이션 마스크만 반환
    
    Args:
        image: OpenCV 이미지 (BGR)
        model: YOLO 모델 인스턴스
        conf_threshold: 탐지 신뢰도 임계값
        target_class: 탐지할 클래스 ID
        
    Returns:
        mask: 세그멘테이션 마스크 (H, W), 0~255 또는 None
    """
    try:
        results = model(image, verbose=False)
        r = results[0]

        # 가장 신뢰도 높은 탐지 찾기
        best_detection = None
        max_confidence = 0.0

        for i, box in enumerate(r.boxes):
            conf = float(box.conf[0])
            cls = int(box.cls[0])

            if cls == target_class and conf >= conf_threshold and conf > max_confidence:
                best_detection = i
                max_confidence = conf

        if best_detection is None:
            return None

        # 마스크 추출 및 리사이즈
        mask = r.masks.data[best_detection].cpu().numpy()
        mask = (mask * 255).astype("uint8")

        h, w = image.shape[:2]
        mask = cv2.resize(mask, (w, h))

        return mask

    except Exception as e:
        logger.warning(f"마스크 추출 실패: {e}")
        return None


def apply_mask_to_image(
        image: np.ndarray,
        mask: np.ndarray
) -> np.ndarray:
    """
    이미지에 마스크 적용
    
    Args:
        image: 원본 이미지 (BGR)
        mask: 마스크 이미지 (grayscale, 0~255)
        
    Returns:
        masked_image: 마스크 적용된 이미지
    """
    # 마스크 크기 맞추기
    if mask.shape[:2] != image.shape[:2]:
        h, w = image.shape[:2]
        mask = cv2.resize(mask, (w, h))

    # 3채널로 변환
    if len(mask.shape) == 2:
        mask_3c = cv2.merge([mask, mask, mask])
    else:
        mask_3c = mask

    # 마스크 적용
    return cv2.bitwise_and(image, mask_3c)
