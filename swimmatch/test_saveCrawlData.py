import os
import django

# Django 설정 초기화
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'swimmatch.settings')
django.setup()

from catalog.services import run_crawl_and_save
from catalog.models import Swimsuit


def test_crawl_basic():
    """기본 크롤링 테스트"""
    print("=" * 50)
    print("🚀 기본 크롤링 테스트 시작")
    print("=" * 50)

    url = "https://swim.co.kr/categories/918698/products?childCategoryNo=919173&brands=%255B43160582%255D"

    result = run_crawl_and_save(url)

    print("\n" + "=" * 50)
    print("✅ 테스트 결과")
    print("=" * 50)
    print(f"신규 저장: {result['created']}개")
    print(f"스킵: {result['skipped']}개")
    print(f"오류: {len(result['errors'])}개")
    # print(f"총 처리:  {result['total']}개")

    if result['errors']:
        print("\n⚠️ 오류 목록:")
        for i, error in enumerate(result['errors'], 1):
            print(f"  {i}. {error}")

    return result


def test_db_check():
    """DB에 저장된 데이터 확인"""
    print("\n" + "=" * 50)
    print("📊 DB 저장 데이터 확인")
    print("=" * 50)

    Swimsuit_count = Swimsuit.objects.count()
    print(f"\n전체 수영복 수:  {Swimsuit_count}개")

    # 최근 저장된 10개
    recent = Swimsuit.objects.order_by('-created_at')[:10]

    print("\n최근 저장된 상품 (상위 10개):")
    for i, item in enumerate(recent, 1):
        print(f"\n  {i}. {item.brand} - {item.name}")
        print(f"     HEX: {item.dominant_color_hex}")
        print(f"     LAB: {item.dominant_lab}")
        print(f"     팔레트: {item.palette}")
        print(f"     가격: {item.price}원")

    return Swimsuit_count


def test_color_classification():
    """색상 분류 테스트"""
    print("\n" + "=" * 50)
    print("🎨 색상 분류 통계")
    print("=" * 50)

    from django.db.models import Count

    color_stats = Swimsuit.objects.values('color_category').annotate(count=Count('id')).order_by('-count')

    print("\n색상별 통계:")
    for stat in color_stats:
        print(f"  {stat['color_category']: 10} :  {stat['count']: 3}개")

    return color_stats


def test_duplicate_check():
    """중복 데이터 확인"""
    print("\n" + "=" * 50)
    print("🔍 중복 데이터 확인")
    print("=" * 50)

    from django.db.models import Count

    duplicates = Swimsuit.objects.values('purchase_link').annotate(count=Count('id')).filter(count__gt=1)

    if duplicates.exists():
        print(f"\n⚠️ 중복 상품 발견: {len(duplicates)}개")
        for dup in duplicates:
            print(f"  - {dup['purchase_link']}:  {dup['count']}개")
    else:
        print("\n✅ 중복 상품 없음")

    return duplicates


def test_image_and_palette():
    """이미지 및 팔레트 데이터 확인"""
    print("\n" + "=" * 50)
    print("🖼️  이미지 및 팔레트 데이터 확인")
    print("=" * 50)

    items = Swimsuit.objects.filter(palette__isnull=False).exclude(palette=[])[: 5]

    print(f"\n팔레트 데이터 있는 상품 (샘플 5개):")
    for i, item in enumerate(items, 1):
        print(f"\n  {i}. {item.brand} - {item.name}")
        print(f"     이미지: {item.image[: 50]}...")
        print(f"     팔레트 색상: {item.palette}")

    return items


def run_all_tests():
    """모든 테스트 실행"""
    print("\n\n")
    print("🧪 " * 25)
    print("수영복 크롤링 및 저장 통합 테스트")
    print("🧪 " * 25)

    # 1. 크롤링 테스트
    result = test_crawl_basic()

    # 2. DB 데이터 확인
    test_db_check()

    # 3. 색상 분류 통계
    # test_color_classification()

    # 4. 중복 확인
    # test_duplicate_check()

    # 5. 팔레트 데이터 확인
    # test_image_and_palette()

    print("\n\n" + "=" * 50)
    print("✅ 모든 테스트 완료!")
    print("=" * 50 + "\n")


if __name__ == "__main__":
    run_all_tests()