# SwimMatch (Django) — 색상 기반 수영복/수모 추천 서비스 (프로젝트 뼈대)

개요
- Django 프로젝트로 수영복(swimsuit)과 수모(swimcap) 이미지를 DB에 저장하고,
  이미지에서 색상(지배색/팔레트)을 추출하여 색상 거리(Lab 기준)로 추천하는 간단한 서비스입니다.
- PyCharm에서 개발/디버깅하도록 구성되어 있습니다.

주요 라이브러리
- Pillow, OpenCV (opencv-python), numpy
- scikit-learn (KMeans)
- scikit-image (RGB ↔ Lab 변환)
- Django REST Framework (간단한 API)

설치
1. 가상환경 생성 (권장)
   python -m venv .venv
   source .venv/bin/activate  # mac/linux
   .venv\Scripts\activate     # windows

2. 패키지 설치
   pip install -r requirements.txt

3. 마이그레이션
   python manage.py migrate

이미지 임포트 (이미 크롤링/저장해둔 경우)
- images/ 폴더에 swimsuits/ 와 swimcaps/ 로 구분해서 이미지를 넣어둡니다.
  예: /path/to/images/swimsuits/*.jpg, /path/to/images/swimcaps/*.jpg

- import 명령 실행:
  python manage.py import_images --type swimsuits --dir /path/to/images/swimsuits
  python manage.py import_images --type swimcaps --dir /path/to/images/swimcaps

API 예시
- 모든 수영복 목록: GET /api/swimsuits/
- 특정 수영복에 대해 추천 수모: GET /api/swimsuits/{id}/recommend_caps/

색상 알고리즘
- 현재 색상 추출: KMeans(n_colors=3)로 dominant 및 palette 추출
- 추천: CIE Lab 공간에서 유클리드 거리 기반으로 가장 가까운 수모를 추천
- 확장: 보색(complementary), 유사색(analogous), 삼원색(triadic) 등의 알고리즘 인터페이스는 utils/color_utils.py에 자리 잡고 있습니다. 필요하시면 원하는 알고리즘을 선택/구현해 드립니다.

PyCharm 설정(간단)
- Interpreter: 프로젝트 가상환경(.venv) 선택
- Run/Debug: manage.py runserver (parameters: runserver 127.0.0.1:8000)

다음 단계 제안(옵션)
- 프론트엔드(React/Vue)와 연동하여 이미지 업로드/추천 UI 구현
- 추천 알고리즘 비교: 보색/유사색/패턴 기반(스트라이프, 플라워 등) 필터 추가
- 색상 정확도 개선: 배경 제거(세그멘테이션) 후 전경 색상만 사용
- 성능: 대량 이미지 처리용 비동기 배치(큐) 도입