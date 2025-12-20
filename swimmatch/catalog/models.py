from django.db import models
from django.db.models import JSONField


class Swimsuit(models.Model):
    """수영복 모델"""
    name = models.CharField(
        max_length=255,
        help_text="수영복 상품명 (예: Nike 경기용 수영복)"
    )
    image = models.ImageField(
        upload_to='swimsuits/',
        help_text="수영복 사진"
    )
    product_url = models.URLField(
        help_text="구매 링크 (쇼핑몰 URL)"
    )
    brand = models.CharField(
        max_length=255,
        help_text="브랜드명 (예: Nike, Speedo)"
    )
    price = models.IntegerField(
        help_text="가격 (예: 89000)"
    )

    # 색상 정보 (색 분류 후 자동 채워짐)
    dominant_color_hex = models.CharField(
        max_length=7,
        blank=True,
        help_text="지배색(대표 색상) — HEX 형식 (예: #FF0000)"
    )
    palette = JSONField(
        default=list,
        blank=True,
        help_text="팔레트(추출된 색상 목록) — JSON 형식 (예: ['#FF0000', '#FFFFFF'])"
    )
    dominant_lab = JSONField(
        null=True,
        blank=True,
        help_text="Lab 색공간 좌표 — JSON 형식 (예: [50.0, 60.0, 30.0])"
    )

    created_at = models.DateTimeField(
        auto_now_add=True,
        help_text="DB에 저장된 시간 (자동 기록)"
    )

    def __str__(self):
        return f"{self.name} ({self.brand}) - {self.dominant_color_hex}"

class SwimCap(models.Model):
    """수모 모델"""
    name = models.CharField(
        max_length=255,
        help_text="수모 상품명"
    )
    image = models.ImageField(
        upload_to='swimcaps/',
        help_text="수모 사진"
    )
    product_url = models.URLField(
        help_text="구매 링크"
    )
    brand = models.CharField(
        max_length=255,
        help_text="브랜드명"
    )
    price = models.IntegerField(
        help_text="가격"
    )

    # 색상 정보
    dominant_color_hex = models.CharField(
        max_length=7,
        blank=True,
        help_text="지배색"
    )
    palette = JSONField(
        default=list,
        blank=True,
        help_text="팔레트"
    )
    dominant_lab = JSONField(
        null=True,
        blank=True,
        help_text="Lab 색공간 좌표"
    )

    created_at = models.DateTimeField(
        auto_now_add=True,
        help_text="DB에 저장된 시간"
    )

    def __str__(self):
        return f"{self.name} ({self.brand}) - {self.dominant_color_hex}"