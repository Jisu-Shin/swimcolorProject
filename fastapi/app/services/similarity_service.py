import cv2
import numpy as np
from app.db import get_all_swimcap_pallete
from pyciede2000 import ciede2000

def hex_to_lab(hex_color):
    """HEX 색상 코드를 LAB 색공간으로 변환"""
    # 1. HEX → RGB
    hex_color = hex_color.lstrip('#')
    rgb = tuple(int(hex_color[i:i + 2], 16) for i in (0, 2, 4))

    # 2. RGB → LAB
    rgb_normalized = np.array(rgb).reshape(1, 1, 3) / 255.0
    lab = cv2.cvtColor(rgb_normalized.astype(np.float32), cv2.COLOR_RGB2LAB)

    # 3. (L, a, b) 형태로 반환
    return lab.reshape(3).tolist()

def color_similarity_ciede(suit_lab, cap_lab):
    """CIEDE2000 색상 유사도 (작을수록 유사)"""
    result = ciede2000(suit_lab, cap_lab)  # 딕셔너리 반환
    return result['delta_E_00']  # 색차 값만 추출

def recommend_swim_caps(db, swimsuit_id, swimsuit_colors):
    """
    수영복 색상 기반 수모 top-n 추천
    """

    # 1. 모든 swimcap 팔레트 조회
    all_swimcap_pallete = get_all_swimcap_pallete(db)

    # 2. 각 swimcap별 cap_info 생성 (swimcap_id 그룹화)
    recommendations = []

    suit_info_list = [
        {'hex_color': hex_color, 'lab_color': hex_to_lab(hex_color)}
        for hex_color in swimsuit_colors
    ]

    # 색상 유사도 필터링을 위한 최소거리
    min_distance = 15.0

    for pallete in all_swimcap_pallete:
        cap_lab_color = hex_to_lab(pallete.colors)

        for suit_info in suit_info_list:
            # 수영복의 각 색상과 비교하여 가장 유사한 것 찾기
            distance = color_similarity_ciede(suit_info['lab_color'], cap_lab_color)
            # print(f"거리계산: {distance}")

            if distance < min_distance:
                recommendations.append({
                    'swimsuitId': swimsuit_id,
                    "swimcapId": pallete.swimcap_id,
                    'suitHexColor': suit_info['hex_color'],
                    'capHexColor': pallete.colors,
                    'similarityScore': round(distance, 4),
                })

    # 유사도 순 정렬
    recommendations.sort(key=lambda x: x['similarityScore'])

    # 동일 swimcap_id 제거 (상위 유사도만 유지)
    seen_ids = set()
    unique_recommendations = []

    for rec in recommendations:
        if rec['swimcapId'] not in seen_ids:
            unique_recommendations.append(rec)
            seen_ids.add(rec['swimcapId'])

    return unique_recommendations[:5]
