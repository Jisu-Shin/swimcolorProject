import cv2
import numpy as np

class SimilarityService :
    def __init__(self):
        self.swimcap_list = []

    def calculate(self, swimsuit_id):
        # swimsuit_id
        
        return self.swimcap_list

    def rgb_to_lab(rgb):
        """RGB를 LAB 색공간으로 변환"""
        rgb_normalized = np.array(rgb).reshape(1, 1, 3) / 255.0
        lab = cv2.cvtColor(rgb_normalized.astype(np.float32), cv2.COLOR_RGB2LAB)
        return lab.reshape(3)

    def color_similarity_lab(self, color1_rgb, color2_rgb):
        """LAB 색공간에서 두 색상 간 유사도 계산 (작을수록 유사)"""
        from scipy.spatial.distance import euclidean
        lab1 = self.rgb_to_lab(color1_rgb)
        lab2 = self.rgb_to_lab(color2_rgb)
        return euclidean(lab1, lab2)

    def recommend_swim_caps(self, swimsuit_id, swimsuit_colors, cap_database, top_n=5):
        """
        수영복 색상 기반 수모 추천

        Parameters:
        - swimsuit_colors: 수영복 주요 색상 [{rgb, hex, ratio}, ...]
        - cap_database: 수모 DB [{id, name, color (RGB), image_url, price}, ...]
        - top_n: 추천할 개수

        Returns:
        - recommendations: 추천 수모 리스트
        """
        recommendations = []

        for cap in cap_database:
            cap_color = cap['color']

            # 수영복의 각 색상과 비교하여 가장 유사한 것 찾기
            min_distance = float('inf')
            best_match = None

            for swim_color in swimsuit_colors:
                distance = self.color_similarity_lab(swim_color['rgb'], cap_color)

                # 색상 비율을 가중치로 고려 (많이 나타난 색상에 더 가중치)
                weighted_distance = distance * (2 - swim_color['ratio'])

                if weighted_distance < min_distance:
                    min_distance = weighted_distance
                    best_match = swim_color

            recommendations.append({
                'swimsuit_id': "123",
                "swimcap_id" : "234",
                'swimsuit_color_hex': '#{:02x}{:02x}{:02x}'.format(*cap_color),
                'swimcap_color_hex': '#{:02x}{:02x}{:02x}'.format(*cap_color),
                'similarity_score': min_distance,
                'matched_swimsuit_color': best_match['hex'],
                'matched_color_ratio': best_match['ratio']
            })

        # 유사도 순 정렬
        recommendations.sort(key=lambda x: x['similarity_score'])

        return recommendations[:top_n]
