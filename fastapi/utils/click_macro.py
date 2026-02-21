import pyautogui
import time
from pynput import mouse, keyboard

# 전역 변수로 좌표 리스트 관리
task_list = []
collecting = False


def on_click(x, y, button, pressed):
    global collecting
    if collecting and pressed and button == mouse.Button.right:
        # pyautogui 좌표계와 맞추기 위해 position() 사용
        curr_x, curr_y = pyautogui.position()
        print(f"   [저장됨] 좌표: ({curr_x}, {curr_y})")
        # 기본 대기시간 1.0초로 자동 저장
        task_list.append([curr_x, curr_y, 1.0])


def collect_coordinates():
    global collecting
    collecting = True
    print("\n--- [1] 좌표 수집 모드 ---")
    print("1. 클릭할 위치를 마우스 왼쪽 버튼으로 클릭하세요.")
    print("2. 클릭할 때마다 리스트에 자동 저장됩니다.")
    print("3. 좌표 수집을 마치려면 키보드 'ESC'를 누르세요.")

    with mouse.Listener(on_click=on_click) as m_listener:
        with keyboard.Listener(on_press=lambda key: key != keyboard.Key.esc) as k_listener:
            k_listener.join()  # ESC 누를 때까지 대기

    collecting = False
    print(f"\n✅ 총 {len(task_list)}개의 좌표가 저장되었습니다.")


def run_macro():
    if not task_list:
        print("\n⚠️ 저장된 좌표가 없습니다! 1번을 먼저 눌러주세요.")
        return

    print("\n--- [2] 매크로 실행 설정 ---")
    try:
        repeat = int(input("전체 사이클 반복 횟수를 입력하세요: "))

        print("\n각 클릭 사이의 대기 시간을 수정하시겠습니까? (Enter 누르면 기본 1초)")
        new_delay = input("대기 시간(초): ").strip()
        if new_delay:
            for task in task_list:
                task[2] = float(new_delay)

        print("\n🚀 2초 후 매크로를 시작합니다!")
        print("(중단하려면 마우스를 화면 왼쪽 상단 구석으로 던지세요)")
        time.sleep(2)

        try:
            for i in range(repeat):
                print(f"   [회차 {i + 1}/{repeat}] 실행 중...")
                for x, y, delay in task_list:
                    pyautogui.click(int(x), int(y))
                    time.sleep(delay)
        except pyautogui.FailSafeException:
            print("\n🛑 안전 장치 작동: 마우스가 구석에 감지되어 중단되었습니다.")
        except KeyboardInterrupt:
            print("\n🛑 사용자가 키보드로 중단하였습니다.")

        print("\n✅ 매크로 실행이 완료되었습니다.")

    except ValueError:
        print("⚠️ 숫자만 입력해주세요.")


def main_menu():
    while True:
        print("\n" + "=" * 30)
        print("   Mac 파이썬 매크로 도구")
        print("=" * 30)
        print(" 1. 좌표 수집하기 (클릭으로 추가)")
        print(" 2. 매크로 실행하기 (저장된 좌표로)")
        print(" 3. 저장된 좌표 리스트 초기화")
        print(" 4. 종료")
        print("-" * 30)

        choice = input("원하는 메뉴 번호를 입력하세요: ").strip()

        if choice == '1':
            collect_coordinates()
        elif choice == '2':
            run_macro()
        elif choice == '3':
            task_list.clear()
            print("\n🧹 리스트가 초기화되었습니다.")
        elif choice == '4':
            print("\n프로그램을 종료합니다.")
            break
        else:
            print("\n⚠️ 잘못된 입력입니다.")


if __name__ == "__main__":
    main_menu()