"""
색상 추출 결과 시각화 유틸리티

개발 및 디버깅 목적으로 색상 추출 결과를 시각화합니다.
프로덕션 환경에서는 사용하지 않습니다.
"""

import cv2
import numpy as np
from typing import List, Dict, Any, Optional
import matplotlib.pyplot as plt
import logging

logger = logging.getLogger(__name__)


def visualize_color_extraction(
        original_image: np.ndarray,
        cropped_image: np.ndarray,
        colors: List[Dict[str, Any]],
        mask: Optional[np.ndarray] = None,
        save_path: Optional[str] = None,
        show: bool = True
) -> None:
    """
    색상 추출 결과 시각화
    
    6개 패널로 구성:
    1. 원본 이미지
    2. 마스크 적용된 객체
    3. 세그멘테이션 마스크
    4. 추출된 색상 팔레트
    5. 원본 + 마스크 오버레이
    6. (여유 공간)
    
    Args:
        original_image: 원본 이미지 (BGR)
        cropped_image: 마스크 적용된 객체 이미지 (BGR)
        colors: 추출된 색상 리스트
        mask: 세그멘테이션 마스크 (grayscale, 0~255)
        save_path: 저장 경로 (선택)
        show: plt.show() 호출 여부
    """
    try:
        fig = plt.figure(figsize=(16, 8))

        # 1. 원본 이미지
        ax1 = plt.subplot(2, 3, 1)
        ax1.imshow(cv2.cvtColor(original_image, cv2.COLOR_BGR2RGB))
        ax1.set_title('Original Image', fontsize=12, fontweight='bold')
        ax1.axis('off')

        # 2. 마스크 적용된 객체
        ax2 = plt.subplot(2, 3, 2)
        ax2.imshow(cv2.cvtColor(cropped_image, cv2.COLOR_BGR2RGB))
        ax2.set_title('Segmented Object', fontsize=12, fontweight='bold')
        ax2.axis('off')

        # 3. 세그멘테이션 마스크
        ax3 = plt.subplot(2, 3, 3)
        if mask is not None:
            ax3.imshow(mask, cmap='gray')
            ax3.set_title('Segmentation Mask', fontsize=12, fontweight='bold')
        else:
            ax3.text(0.5, 0.5, 'No Mask', ha='center', va='center')
        ax3.axis('off')

        # 4. 색상 팔레트
        ax4 = plt.subplot(2, 3, 4)
        _draw_color_palette(ax4, colors)

        # 5. 오버레이
        ax5 = plt.subplot(2, 3, 5)
        if mask is not None:
            overlay = _create_overlay(original_image, mask)
            ax5.imshow(cv2.cvtColor(overlay, cv2.COLOR_BGR2RGB))
            ax5.set_title('Original + Mask Overlay', fontsize=12, fontweight='bold')
        else:
            ax5.text(0.5, 0.5, 'No Overlay', ha='center', va='center')
        ax5.axis('off')

        # 6. 색상 통계
        ax6 = plt.subplot(2, 3, 6)
        _draw_color_stats(ax6, colors)

        plt.tight_layout()

        # 저장
        if save_path:
            plt.savefig(save_path, dpi=150, bbox_inches='tight')
            logger.info(f"시각화 저장: {save_path}")

        # 표시
        if show:
            plt.show()
        else:
            plt.close()

    except Exception as e:
        logger.error(f"시각화 생성 실패: {e}", exc_info=True)
        plt.close()


def _draw_color_palette(ax, colors: List[Dict[str, Any]]) -> None:
    """
    색상 팔레트 그리기
    
    Args:
        ax: matplotlib axes
        colors: 색상 리스트
    """
    if not colors:
        ax.text(0.5, 0.5, 'No Colors', ha='center', va='center')
        ax.axis('off')
        return

    n_colors = len(colors)
    color_blocks = np.zeros((100, n_colors * 100, 3), dtype=np.uint8)

    for i, color_info in enumerate(colors):
        # 색상 블록 채우기
        color_blocks[:, i * 100:(i + 1) * 100] = color_info['rgb']

        # HEX 코드 표시
        mid_x = i * 100 + 50
        ax.text(
            mid_x, 30,
            color_info['hex'],
            ha='center', va='center',
            fontsize=10, fontweight='bold', color='white',
            bbox=dict(boxstyle='round', facecolor='black', alpha=0.7)
        )

        # 비율 표시
        ax.text(
            mid_x, 70,
            f"{color_info['ratio'] * 100:.1f}%",
            ha='center', va='center',
            fontsize=9, color='white',
            bbox=dict(boxstyle='round', facecolor='black', alpha=0.7)
        )

    ax.imshow(color_blocks)
    ax.set_title('Extracted Color Palette', fontsize=12, fontweight='bold')
    ax.axis('off')


def _create_overlay(
        image: np.ndarray,
        mask: np.ndarray,
        color: tuple = (0, 255, 0),
        alpha: float = 0.5
) -> np.ndarray:
    """
    원본 이미지에 마스크 오버레이 생성
    
    Args:
        image: 원본 이미지 (BGR)
        mask: 마스크 (grayscale, 0~255)
        color: 오버레이 색상 (BGR)
        alpha: 투명도 (0.0 ~ 1.0)
        
    Returns:
        overlay: 오버레이된 이미지
    """
    overlay = image.copy()

    # 마스크 영역에 색상 적용
    mask_bool = mask > 127
    overlay[mask_bool] = (
            overlay[mask_bool] * (1 - alpha) +
            np.array(color) * alpha
    ).astype(np.uint8)

    return overlay


def _draw_color_stats(ax, colors: List[Dict[str, Any]]) -> None:
    """
    색상 통계 표시
    
    Args:
        ax: matplotlib axes
        colors: 색상 리스트
    """
    if not colors:
        ax.text(0.5, 0.5, 'No Statistics', ha='center', va='center')
        ax.axis('off')
        return

    # 텍스트 정보 생성
    stats_text = "Color Statistics\n" + "=" * 30 + "\n\n"
    stats_text += f"Total Colors: {len(colors)}\n\n"

    for i, color in enumerate(colors, 1):
        stats_text += f"{i}. {color['hex']}\n"
        stats_text += f"   RGB: {tuple(color['rgb'])}\n"
        stats_text += f"   Ratio: {color['ratio'] * 100:.2f}%\n"
        stats_text += f"   Pixels: {color.get('count', 'N/A')}\n\n"

    ax.text(
        0.05, 0.95,
        stats_text,
        ha='left', va='top',
        fontsize=9,
        family='monospace',
        transform=ax.transAxes
    )
    ax.set_title('Statistics', fontsize=12, fontweight='bold')
    ax.axis('off')


def save_color_palette_only(
        colors: List[Dict[str, Any]],
        save_path: str,
        width: int = 600,
        height: int = 100
) -> None:
    """
    색상 팔레트만 저장
    
    Args:
        colors: 색상 리스트
        save_path: 저장 경로
        width: 이미지 너비
        height: 이미지 높이
    """
    if not colors:
        logger.warning("저장할 색상이 없습니다.")
        return

    n_colors = len(colors)
    color_width = width // n_colors

    palette = np.zeros((height, width, 3), dtype=np.uint8)

    for i, color_info in enumerate(colors):
        start_x = i * color_width
        end_x = (i + 1) * color_width if i < n_colors - 1 else width
        palette[:, start_x:end_x] = color_info['rgb']

    # BGR로 변환 후 저장
    palette_bgr = cv2.cvtColor(palette, cv2.COLOR_RGB2BGR)
    cv2.imwrite(save_path, palette_bgr)

    logger.info(f"팔레트 저장: {save_path}")


def compare_extractions(
        images: List[np.ndarray],
        all_colors: List[List[Dict[str, Any]]],
        titles: Optional[List[str]] = None,
        save_path: Optional[str] = None
) -> None:
    """
    여러 이미지의 색상 추출 결과 비교
    
    Args:
        images: 이미지 리스트
        all_colors: 각 이미지별 색상 리스트
        titles: 각 이미지 제목 (선택)
        save_path: 저장 경로 (선택)
    """
    n_images = len(images)

    if n_images == 0:
        logger.warning("비교할 이미지가 없습니다.")
        return

    fig, axes = plt.subplots(2, n_images, figsize=(5 * n_images, 10))

    if n_images == 1:
        axes = axes.reshape(-1, 1)

    for i, (img, colors) in enumerate(zip(images, all_colors)):
        # 원본 이미지
        axes[0, i].imshow(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
        title = titles[i] if titles and i < len(titles) else f"Image {i + 1}"
        axes[0, i].set_title(title, fontsize=12, fontweight='bold')
        axes[0, i].axis('off')

        # 색상 팔레트
        _draw_color_palette(axes[1, i], colors)

    plt.tight_layout()

    if save_path:
        plt.savefig(save_path, dpi=150, bbox_inches='tight')
        logger.info(f"비교 이미지 저장: {save_path}")

    plt.show()
