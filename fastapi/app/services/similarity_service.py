"""
수영복-수모 색상 유사도 기반 추천 서비스

이 모듈은 CIEDE2000 색차 알고리즘을 사용하여 수영복 색상과 
가장 유사한 수모를 추천합니다.

버전관리 규칙

MAJOR.MINOR.PATCH
  1  .  0  .  0

MAJOR (1.x.x): 호환성이 깨지는 큰 변경
예: 알고리즘 완전 변경, 반환 형식 변경

MINOR (x.1.x): 기능 추가, 호환 가능한 변경
예: 새로운 파라미터 추가, 임계값 조정

PATCH (x.x.1): 버그 수정, 작은 개선
예: 에러 처리 개선, 성능 최적화

버전 히스토리:
- v1.0.0 (2025-01-15): 초기 CIEDE2000 기반 추천 알고리즘
"""

import cv2
import numpy as np
from typing import List, Dict, Any
from sqlalchemy.orm import Session
from app.db import get_all_swimcap_pallete
from pyciede2000 import ciede2000

# ============================================================================
# 알고리즘 설정 및 버전 관리
# ============================================================================

class SimilarityConfig:
    """
    유사도 계산 알고리즘 설정
    
    알고리즘을 수정할 때는 VERSION을 반드시 업데이트하세요.
    """
    # 버전 관리 (코드 변경 시 반드시 업데이트)
    VERSION = "1.0.0"               # Semantic version (추천)

    # 알고리즘 설정
    MIN_DISTANCE_THRESHOLD = 8.0    # 최소 색차 임계값 (작을수록 비슷)
    MAX_RECOMMENDATIONS = 6         # 최대 추천 개수
    COLOR_SPACE = "CIEDE2000"       # 사용 중인 색공간/알고리즘

# ============================================================================
# 색공간 변환 함수
# ============================================================================

def hex_to_lab(hex_color: str) -> List[float]:
    """
    HEX 색상 코드를 LAB 색공간으로 변환
    
    Args:
        hex_color: HEX 색상 코드 (예: "#FF5733" 또는 "FF5733")
        
    Returns:
        LAB 색공간 값 리스트 [L, a, b]
        
    Raises:
        ValueError: 유효하지 않은 HEX 색상 코드인 경우
    """
    try:
        # 1. HEX → RGB
        hex_color = hex_color.lstrip('#')
        if len(hex_color) != 6:
            raise ValueError(f"Invalid hex color: {hex_color}")

        rgb = tuple(int(hex_color[i:i + 2], 16) for i in (0, 2, 4))

        # 2. RGB → LAB (OpenCV 사용)
        rgb_normalized = np.array(rgb).reshape(1, 1, 3) / 255.0
        lab = cv2.cvtColor(rgb_normalized.astype(np.float32), cv2.COLOR_RGB2LAB)

        # 3. (L, a, b) 형태로 반환
        return lab.reshape(3).tolist()
    except Exception as e:
        raise ValueError(f"Failed to convert hex to LAB: {hex_color}") from e


# ============================================================================
# 색상 유사도 계산
# ============================================================================

def color_similarity_ciede(suit_lab: List[float], cap_lab: List[float]) -> float:
    """
    CIEDE2000 알고리즘을 사용한 색상 유사도 계산
    
    Args:
        suit_lab: 수영복 LAB 색상 값 [L, a, b]
        cap_lab: 수모 LAB 색상 값 [L, a, b]
        
    Returns:
        색차(ΔE) 값 - 작을수록 유사함 (0 = 완전 동일)
        
    Note:
        CIEDE2000은 인간의 시각적 인지에 가장 근접한 색차 계산 방법입니다.
        일반적으로 ΔE < 1: 인간이 구분 불가, ΔE < 3: 매우 유사
    """
    result = ciede2000(suit_lab, cap_lab)  # 딕셔너리 반환
    return result['delta_E_00']  # 색차 값만 추출


# ============================================================================
# 추천 함수
# ============================================================================

def recommend_swim_caps(
        db: Session,
        swimsuit_id: str,
        swimsuit_colors: List[str],
        min_distance: float = SimilarityConfig.MIN_DISTANCE_THRESHOLD,
        max_results: int = SimilarityConfig.MAX_RECOMMENDATIONS
) -> List[Dict[str, Any]]:
    """
    수영복 색상 기반 수모 추천
    
    Args:
        db: 데이터베이스 세션
        swimsuit_id: 수영복 ID
        swimsuit_colors: 수영복 HEX 색상 리스트 (예: ["#FF5733", "#3498DB"])
        min_distance: 유사도 필터링 임계값 (기본: 8.0, 작을수록 엄격)
        max_results: 최대 추천 개수 (기본: 6)
        
    Returns:
        추천 수모 리스트. 각 항목은 다음 필드를 포함:
        - swimsuitId: 수영복 ID
        - swimcapId: 수모 ID
        - suitHexColor: 매칭된 수영복 색상 (HEX)
        - capHexColor: 수모 색상 (HEX)
        - similarityScore: 유사도 점수 (낮을수록 유사)
        - algorithmVersion: 사용된 알고리즘 버전
        
    Example:
        >>> recommend_swim_caps(db, "SW001", ["#FF5733", "#3498DB"])
        [
            {
                'swimsuitId': 'SW001',
                'swimcapId': 'SC123',
                'suitHexColor': '#FF5733',
                'capHexColor': '#FF6347',
                'similarityScore': 2.3456,
                'algorithmVersion': '1.0.0'
            },
            ...
        ]
    """
    # 1. 모든 수모 팔레트 조회
    all_swimcap_pallete = get_all_swimcap_pallete(db)

    # 2. 수영복 색상 정보를 LAB로 변환
    suit_info_list = []
    for hex_color in swimsuit_colors:
        try:
            suit_info_list.append({
                'hex_color': hex_color,
                'lab_color': hex_to_lab(hex_color)
            })
        except ValueError as e:
            # 유효하지 않은 색상은 스킵
            print(f"Warning: Skipping invalid color {hex_color}: {e}")
            continue

    if not suit_info_list:
        return []

    # 3. 각 수모와 유사도 계산
    recommendations = []

    for pallete in all_swimcap_pallete:
        try:
            cap_lab_color = hex_to_lab(pallete.colors)
        except ValueError:
            # 유효하지 않은 수모 색상은 스킵
            continue

        for suit_info in suit_info_list:
            # 수영복의 각 색상과 비교
            distance = color_similarity_ciede(suit_info['lab_color'], cap_lab_color)

            # 임계값 이하인 경우만 추천 목록에 추가
            if distance < min_distance:
                recommendations.append({
                    'swimsuitId': swimsuit_id,
                    'swimcapId': pallete.swimcap_id,
                    'suitHexColor': suit_info['hex_color'],
                    'capHexColor': pallete.colors,
                    'similarityScore': round(distance, 4),
                    'algorithmVersion': SimilarityConfig.VERSION  # 🆕 버전 정보 추가
                })

    # 4. 유사도 순 정렬 (낮은 값이 더 유사)
    recommendations.sort(key=lambda x: x['similarityScore'])

    # 5. 동일 swimcap_id 중복 제거 (가장 유사한 것만 유지)
    seen_ids = set()
    unique_recommendations = []

    for rec in recommendations:
        if rec['swimcapId'] not in seen_ids:
            unique_recommendations.append(rec)
            seen_ids.add(rec['swimcapId'])

    # 6. 최대 개수만큼 반환
    return unique_recommendations[:max_results]


# ============================================================================
# 버전 정보 조회 함수
# ============================================================================

def get_algorithm_info() -> Dict[str, Any]:
    """
    현재 사용 중인 알고리즘 정보 반환
    
    Returns:
        알고리즘 정보 딕셔너리
    """
    return {
        'version': SimilarityConfig.VERSION,
        'colorSpace': SimilarityConfig.COLOR_SPACE,
        'minDistanceThreshold': SimilarityConfig.MIN_DISTANCE_THRESHOLD,
        'maxRecommendations': SimilarityConfig.MAX_RECOMMENDATIONS,
        'description': 'CIEDE2000 기반 색상 유사도 추천 알고리즘'
    }
