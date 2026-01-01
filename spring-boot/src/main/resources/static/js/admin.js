/** 1. 공통 폴링 함수 (Promise 기반)
 * 특정 카테고리가 COMPLETED가 될 때까지 2초마다 확인합니다.
 */
function waitForCompletion(category) {
    return new Promise((resolve, reject) => {
        const check = () => {
            oper.ajax("GET", null, `/api/admin/crawlStatus/${category}`, function(status) {
                console.log(`${category} 현재 상태: ${status}`);

                if (status === "COMPLETED") {
                    resolve(); // 약속 이행!
                } else if (status === "FAILED") {
                    reject(new Error("크롤링 중 서버 에러 발생")); // 약속 파기!
                } else {
                    setTimeout(check, 5000); // 4초 뒤 재시도
                }
            });
        };
        check();
    });
}

/**
 * 2. 수영복 크롤링 함수
 */
async function runSwimsuitCrawl() {
    const $btn = $('#btn-crawl-swimsuit'); // ID로 직접 지정
    const url = $('#swimsuitUrl').val();

    if(oper.isEmpty(url)) return alert("수영복 URL을 입력해 주세요.");

    // [Step 1] UI 잠금
    $btn.prop('disabled', true).text("크롤링 중...");
    $('#swimsuitUrl').prop('disabled', true);

    try {
        // [Step 2] 크롤링 시작 요청 (POST)
        // oper.ajax가 내부적으로 Promise를 반환하지 않는다면 아래처럼 감쌀 수 있어요.
        await new Promise((res) => oper.ajax("POST", {url: url}, "/api/admin/crawlSwimsuits", res));

        // [Step 3] 완료될 때까지 대기
        await waitForCompletion('SWIMSUIT');

        alert("수영복 크롤링이 완료되었습니다!");
    } catch (error) {
        alert("크롤링 실패: " + error.message);
    } finally {
        // [Step 4] 무조건 버튼 복구
        $btn.prop('disabled', false).text("수영복 크롤링");
        $('#swimsuitUrl').prop('disabled', false).val('');
    }
}

/**
 * 3. 수모 크롤링 함수 (복사해서 ID와 카테고리만 바꾸면 끝!)
 */
async function runSwimcapCrawl() {
    const $btn = $('#btn-crawl-swimcap');
    const url = $('#swimcapUrl').val();

    if(oper.isEmpty(url)) return alert("수모 URL을 입력해 주세요.");

    $btn.prop('disabled', true).text("크롤링 중...");
    $('#swimcapUrl').prop('disabled', true);

    try {
        await new Promise((res) => oper.ajax("POST", {url: url}, "/api/admin/crawlSwimcaps", res));
        await waitForCompletion('SWIMCAP');
        alert("수모 크롤링이 완료되었습니다!");
    } catch (error) {
        alert("실패: " + error.message);
    } finally {
        $btn.prop('disabled', false).text("수모 크롤링");
        $('#swimcapUrl').prop('disabled', false).val('');
    }
}

var oper = {
    // 값이 비어있는지 체크
    isEmpty : function(value) {
        return (value == "" || value == null || value == undefined);
    },

    // 공통 AJAX 함수
    ajax : function(type, data, url, callback) {

        // 1. Spring Security CSRF 토큰 가져오기 (나중을 위해 미리 세팅)
        // HTML 메타 태그에 csrf 정보가 있다면 가져옵니다.
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        // GET이면 쿼리스트링 처리
        if (type.toUpperCase() === "GET" && data && Object.keys(data).length > 0) {
            const queryString = new URLSearchParams(data).toString();
            url += (url.includes("?") ? "&" : "?") + queryString;
            data = null;
        }

        $.ajax({
            type: type,
            url: url,
            contentType: 'application/json; charset=utf-8',
            data: (type.toUpperCase() === "GET") ? null : JSON.stringify(data),
            // 2. 요청 헤더에 CSRF 토큰 추가 (시큐리티 대응)
            beforeSend: function(xhr) {
                if (csrfToken && csrfHeader) {
                    xhr.setRequestHeader(csrfHeader, csrfToken);
                }
            }
        })
        .done(function(response) {
            if (callback) callback(response);
        })
        .fail(function(xhr, status, error) {
            console.error('요청 실패:', xhr);
            let errMsg = xhr.responseJSON?.message || xhr.responseText || error || '알 수 없는 오류';

            if (xhr.responseJSON?.errors) {
                let fieldErrors = xhr.responseJSON.errors;
                let errorDetails = '\n';
                for (let field in fieldErrors) {
                    errorDetails += `- ${fieldErrors[field]}\n`;
                }
                errMsg += errorDetails;
            }
            alert("에러 발생: " + errMsg);
        });
    }
};