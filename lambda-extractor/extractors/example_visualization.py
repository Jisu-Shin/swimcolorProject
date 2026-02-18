"""
시각화 사용 예시

개발 환경에서 색상 추출 결과를 시각화하는 방법을 보여줍니다.
"""

import asyncio
import cv2
from .color_extractor_parallel import ColorExtractorParallel
from .visualization_utils import (
    visualize_color_extraction,
    save_color_palette_only,
    compare_extractions
)
from .yolo_utils import get_segmentation_mask
from .yolo_utils import crop_swimsuit_only, apply_mask


async def example_single_image(model_path, image_path):
    """단일 이미지 시각화 예시"""
    print("=" * 60)
    print("단일 이미지 색상 추출 및 시각화")
    print("=" * 60)

    print(f"모델 경로: {model_path}")

    # 1. 색상 추출기 초기화
    extractor = ColorExtractorParallel(str(model_path))

    # 2. 이미지 로드
    original = cv2.imread(image_path)
    
    # 3. 색상 추출
    image_urls = [image_path]
    all_colors = await extractor.extract_colors(image_urls)
    
    colors = all_colors[0]
    
    # 4. 마스크 및 크롭된 이미지 생성
    cropped = crop_swimsuit_only(original, extractor.model, 0.5)
    mask = get_segmentation_mask(original, extractor.model, 0.5)
    
    # 5. 시각화
    visualize_color_extraction(
        original_image=original,
        cropped_image=cropped,
        colors=colors,
        mask=mask,
        save_path="/Users/zsu/Downloads/visualization.png",
        show=True
    )
    
    # 6. 팔레트만 저장
    save_color_palette_only(
        colors,
        save_path="/Users/zsu/Downloads/palette.png"
    )


async def example_multiple_images():
    """여러 이미지 비교 시각화 예시"""
    print("=" * 60)
    print("여러 이미지 색상 추출 비교")
    print("=" * 60)
    
    # 1. 추출기 초기화
    extractor = ColorExtractorParallel(settings.yolo_model_path)
    
    # 2. 여러 이미지 로드
    image_paths = [
        "path/to/image1.jpg",
        "path/to/image2.jpg",
        "path/to/image3.jpg"
    ]
    
    images = []
    for path in image_paths:
        img = cv2.imread(path)
        if img is not None:
            # 썸네일 생성
            h, w = img.shape[:2]
            scale = min(640/w, 640/h)
            new_w, new_h = int(w*scale), int(h*scale)
            img = cv2.resize(img, (new_w, new_h))
            images.append(img)
    
    # 3. 색상 추출
    all_colors = await extractor.extract_colors(
        image_paths
    )
    
    # 4. 비교 시각화
    compare_extractions(
        images=images,
        all_colors=all_colors,
        titles=["Product 1", "Product 2", "Product 3"],
        save_path="output/comparison.png"
    )


async def example_custom_visualization():
    """커스텀 시각화 예시"""
    import matplotlib.pyplot as plt
    
    print("=" * 60)
    print("커스텀 시각화")
    print("=" * 60)
    
    # 1. 색상 추출
    extractor = ColorExtractorParallel(settings.yolo_model_path)
    image_path = "path/to/image.jpg"
    
    all_colors = await extractor.extract_colors([image_path])
    colors = all_colors[0]
    
    # 2. 원형 차트로 색상 비율 표시
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))
    
    # 파이 차트
    labels = [c['hex'] for c in colors]
    sizes = [c['ratio'] for c in colors]
    color_values = [tuple(c['rgb']) for c in colors]
    color_values_normalized = [(r/255, g/255, b/255) for r, g, b in color_values]
    
    ax1.pie(
        sizes,
        labels=labels,
        colors=color_values_normalized,
        autopct='%1.1f%%',
        startangle=90
    )
    ax1.set_title('Color Distribution')
    
    # 막대 차트
    ax2.barh(
        range(len(colors)),
        [c['ratio'] * 100 for c in colors],
        color=color_values_normalized
    )
    ax2.set_yticks(range(len(colors)))
    ax2.set_yticklabels([c['hex'] for c in colors])
    ax2.set_xlabel('Ratio (%)')
    ax2.set_title('Color Ratios')
    
    plt.tight_layout()
    plt.savefig('output/custom_viz.png', dpi=150)
    plt.show()


async def main():
    """메인 함수"""
    # 원하는 예시 선택
    model_path = "../../" + settings.yolo_model_path
    image_path = '/Users/zsu/MyProject/크롤링 사진/swimsuit_0206_02/0071_스웨이브_세레니티 크.jpg'


    await example_single_image(model_path, image_path)
    # await example_multiple_images()
    # await example_custom_visualization()


if __name__ == '__main__':
    asyncio.run(main())
