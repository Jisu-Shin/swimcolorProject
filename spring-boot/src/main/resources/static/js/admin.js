/**
 * [A] 페이지 초기화 및 상태 복구 로직
 */
var main = {
    init: function() {
        const _this = this;
        _this.loadActiveMenu();

        $('.nav-menu a').on('click', function () {
            _this.activate($(this));
        });

        // 페이지 로드 시 진행 중인 작업이 있는지 서버에 확인
        checkCurrentStatus();

        $('.btn-errorColorRecommend').on('click', function() {
            errorColorRecommend($(this));
        });

        $('.btn-errorModelExtract').on('click', function() {
            errorModelExtract($(this));
        });
    },

    activate: function (element) {
        $('.nav-menu a').removeClass('active');
        element.addClass('active');
    },

    loadActiveMenu: function () {
        const currentPath = window.location.pathname;
        $('.nav-menu a').each(function () {
            if (currentPath === $(this).attr('href')) {
                $(this).addClass('active');
            }
        });
    }
};

/**
 * [B] 크롤링 상태 관리 엔진
 */

// 1. 현재 진행 중인 작업 복구 (새로고침 대응)
async function checkCurrentStatus() {
    const categories = ['SWIMSUIT', 'SWIMCAP'];

    for (const category of categories) {
        oper.ajax("GET", null, `/api/crawling/status/${category}`, async function(status) {
            if (status === "RUNNING") {
                console.log(`[복구] ${category} 작업이 진행 중입니다.`);

                const isSwimsuit = (category === 'SWIMSUIT');
                const $btn = isSwimsuit ? $('#btn-crawl-swimsuit') : $('#btn-crawl-swimcap');
                const $input = isSwimsuit ? $('#swimsuitUrl') : $('#swimcapUrl');
                const storageKey = isSwimsuit ? 'lastSwimsuitUrl' : 'lastSwimcapUrl';
                const $stopBtnId = isSwimsuit ? $('#btn-stop-swimsuit') : $('#btn-stop-swimcap');

                // UI 잠금 및 대기 시작
                $btn.prop('disabled', true).text("수집 중(복구됨)...");
                $stopBtnId.prop('hidden',false);

                $input.prop('disabled', true);

                const savedUrl = localStorage.getItem(storageKey);
                if (savedUrl) {
                    $input.val(savedUrl);
                }

                try {
                    await waitForCompletion(category);
                    alert(`${category} 크롤링이 완료되었습니다!`);
                } catch (e) {
                    alert(`${category} 작업 중 문제가 발견되었습니다.`);
                } finally {
                    // 완료 후 정리
                    $btn.prop('disabled', false).text(isSwimsuit ? "수영복 크롤링" : "수모 크롤링");
                    $stopBtnId.prop('hidden', true);

                    $input.prop('disabled', false).val('');
                    localStorage.removeItem(isSwimsuit ? 'lastSwimsuitUrl' : 'lastSwimcapUrl');
                }
            }
        });
    }
}

// 2. 특정 상태가 될 때까지 폴링하는 약속(Promise)
function waitForCompletion(category) {
    return new Promise((resolve, reject) => {
        const check = () => {
            oper.ajax("GET", null, `/api/crawling/status/${category}`, function(status) {
                if (status === "COMPLETED") {
                    resolve();
                } else if (status === "FAILED") {
                    reject(new Error("서버에서 작업 실패 응답을 받았습니다."));
                } else if (status === "IDLE") {
                    // 서버가 IDLE 상태라면 사용자가 중지했거나 작업이 취소된 것
                    console.log(`[중단] 서버가 IDLE 상태이므로 ${category} 폴링을 중단합니다.`);
                    reject(new Error("USER_STOP"));
                } else {
                    setTimeout(check, 5000); // 5초 간격
                }
            });
        };
        check();
    });
}

/**
 * [C] 크롤링 실행 함수
 */

// 수영복 크롤링 실행
async function runSwimsuitCrawl() {
    const $btn = $('#btn-crawl-swimsuit');
    const $btnStop = $('#btn-stop-swimsuit');
    const $input = $('#swimsuitUrl');
    const url = $input.val();

    if(oper.isEmpty(url)) return alert("수영복 URL을 입력해 주세요.");

    // UI 잠금 및 로컬 저장
    $btn.prop('disabled', true).text("크롤링 중...");
    $btnStop.prop('hidden', false);

    $input.prop('disabled', true);
    localStorage.setItem('lastSwimsuitUrl', url);

    try {
        // 1. 비동기 시작 요청 (POST)
        await new Promise((res) => oper.ajax("POST", { crawlingUrl: url }, "/api/admin/crawlSwimsuits", res));

        // 2. 완료 대기
        await waitForCompletion('SWIMSUIT');
        alert("수영복 크롤링 완료!");
    } catch (error) {
        alert("크롤링 실패: " + error.message);
        console.error(error);
    } finally {
        // 3. UI 복구
        $btn.prop('disabled', false).text("수영복 크롤링");
        $btnStop.prop('hidden', true);

        $input.prop('disabled', false).val('');
        localStorage.removeItem('lastSwimsuitUrl');
    }
}

// 수모 크롤링 실행 (수영복과 동일 로직)
async function runSwimcapCrawl() {
    const $btn = $('#btn-crawl-swimcap');
    const $btnStop = $('#btn-stop-swimcap');
    const $input = $('#swimcapUrl');
    const url = $input.val();

    if(oper.isEmpty(url)) return alert("수모 URL을 입력해 주세요.");

    $btn.prop('disabled', true).text("크롤링 중...");
    $btnStop.prop('hidden', false);

    $input.prop('disabled', true);
    localStorage.setItem('lastSwimcapUrl', url);

    try {
        await new Promise((res) => oper.ajax("POST", { crawlingUrl: url }, "/api/admin/crawlSwimcaps", res));
        await waitForCompletion('SWIMCAP');
        alert("수모 크롤링 완료!");
    } catch (error) {
        alert("크롤링 실패: " + error.message);
        console.error(error);
    } finally {
        $btn.prop('disabled', false).text("수모 크롤링");
        $btnStop.prop('hidden', true);
        $input.prop('disabled', false).val('');
        localStorage.removeItem('lastSwimcapUrl');
    }
}

function stopCrawl(category) {
    // 1. 사용자에게 확인 받기
    const message = `${category === 'SWIMSUIT' ? '수영복' : '수모'} 크롤링 조회를 중지하시겠습니까?`;

    if (!confirm(message)) {
        // 사용자가 '취소'를 누르면 여기서 함수 종료
        return;
    }

    const isSwimsuit = (category === 'SWIMSUIT');
    const $btn = isSwimsuit ? $('#btn-crawl-swimsuit') : $('#btn-crawl-swimcap');
    const $input = isSwimsuit ? $('#swimsuitUrl') : $('#swimcapUrl');
    const $stopBtnId = isSwimsuit ? $('#btn-stop-swimsuit') : $('#btn-stop-swimcap');

    oper.ajax("DELETE", null, '/api/crawling/status/'+category);

    localStorage.removeItem(isSwimsuit ? 'lastSwimsuitUrl' : 'lastSwimcapUrl');

    $btn.prop('disabled', false).text(isSwimsuit ? "수영복 크롤링" : "수모 크롤링");
    $input.prop('disabled', false).val('');
    $stopBtnId.prop('hidden', true);
}

function errorColorRecommend($el) {
    const colorMatchId = $el.data('id');
    const algorithmVersion = $el.data('version');

    const data = {
                    'colorMatchId': colorMatchId,
                    'algorithmVersion' : algorithmVersion,
                    'reviewedBy' : 'ADMIN'
                    };
    oper.ajax("POST", data, '/api/colorMatchFeedback/errorColorRecommend', function () {
        alert("[색상 추천 오류] 등록이 완료되었습니다.");
    });
}

function errorModelExtract($el) {
    console.log("모델추출오류");
    const colorMatchId = $el.data('id');
    const algorithmVersion = $el.data('version');

    const data = {
                    'colorMatchId': colorMatchId,
                    'algorithmVersion' : algorithmVersion,
                    'reviewedBy' : 'ADMIN'
                    };
    oper.ajax("POST", data, '/api/colorMatchFeedback/errorModelExtract', function () {
        alert("[모델 추출 오류] 등록이 완료되었습니다.");
    });

}

/**
 * [D] 유틸리티 및 공통 AJAX
 */
var oper = {
    isEmpty: (v) => v == "" || v == null || v == undefined,

    ajax: function(type, data, url, callback) {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        if (type.toUpperCase() === "GET" && data) {
            url += (url.includes("?") ? "&" : "?") + new URLSearchParams(data).toString();
            data = null;
        }

        $.ajax({
            type: type,
            url: url,
            contentType: 'application/json; charset=utf-8',
            data: data ? JSON.stringify(data) : null,
            beforeSend: function(xhr) {
                if (csrfToken && csrfHeader) xhr.setRequestHeader(csrfHeader, csrfToken);
            }
        })
        .done((res) => { if(callback) callback(res); })
        .fail((xhr) => {
            console.error('AJAX Error:', xhr);
            alert("에러 발생: " + (xhr.responseJSON?.message || "서버 통신 실패"));
        });
    }
};

$(document).ready(() => main.init());