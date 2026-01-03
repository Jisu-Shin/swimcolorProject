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
        oper.ajax("GET", null, `/api/admin/crawlStatus/${category}`, async function(status) {
            if (status === "RUNNING") {
                console.log(`[복구] ${category} 작업이 진행 중입니다.`);

                const isSwimsuit = (category === 'SWIMSUIT');
                const $btn = isSwimsuit ? $('#btn-crawl-swimsuit') : $('#btn-crawl-swimcap');
                const $input = isSwimsuit ? $('#swimsuitUrl') : $('#swimcapUrl');
                const storageKey = isSwimsuit ? 'lastSwimsuitUrl' : 'lastSwimcapUrl';

                // UI 잠금 및 대기 시작
                $btn.prop('disabled', true).text("수집 중(복구됨)...");
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
            oper.ajax("GET", null, `/api/admin/crawlStatus/${category}`, function(status) {
                if (status === "COMPLETED") {
                    resolve();
                } else if (status === "FAILED") {
                    reject(new Error("서버에서 작업 실패 응답을 받았습니다."));
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
    const $input = $('#swimsuitUrl');
    const url = $input.val();

    if(oper.isEmpty(url)) return alert("수영복 URL을 입력해 주세요.");

    // UI 잠금 및 로컬 저장
    $btn.prop('disabled', true).text("크롤링 중...");
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
        $input.prop('disabled', false).val('');
        localStorage.removeItem('lastSwimsuitUrl');
    }
}

// 수모 크롤링 실행 (수영복과 동일 로직)
async function runSwimcapCrawl() {
    const $btn = $('#btn-crawl-swimcap');
    const $input = $('#swimcapUrl');
    const url = $input.val();

    if(oper.isEmpty(url)) return alert("수모 URL을 입력해 주세요.");

    $btn.prop('disabled', true).text("크롤링 중...");
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
        $input.prop('disabled', false).val('');
        localStorage.removeItem('lastSwimcapUrl');
    }
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