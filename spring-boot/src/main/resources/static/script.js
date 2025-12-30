var main = {
    init : function() {

        var _this = this;

    }
}

// 수영복 상세 페이지로 이동하는 함수
function goToSwimsuitDetail(id) {
    console.log(id);
    if (oper.isEmpty(id)) {
        alert('상품 정보를 찾을 수 없습니다.');
        return;
    }
    window.location.href = '/swimsuits/' + id;
}

// 수영복 목록 페이지로 이동하는 함수
function goToProducts() {
    window.location.href = '/swimsuits';
}

// 이전 페이지로 돌아가는 함수
function goBack() {
    window.history.back();
}

document.addEventListener('DOMContentLoaded', () => {
    const searchBtn = document.querySelector('.search-btn');
    const searchBar = document.querySelector('.search-bar');
    const searchInput = document.querySelector('.search-bar input');
    const header = document.querySelector('.header');

    searchBtn.addEventListener('click', (e) => {
        // 모바일(767px 이하)에서만 확장 로직 작동
        if (window.innerWidth <= 767) {
            if (!header.classList.contains('search-active')) {
                e.preventDefault(); // 검색 실행 대신 창 확장
                header.classList.add('search-active');
                searchInput.focus(); // 바로 타이핑 가능하게 포커스
            }
        }
    });

    // 검색창 외 영역 클릭 시 다시 축소 (선택 사항)
    document.addEventListener('click', (e) => {
        if (header.classList.contains('search-active') && !searchBar.contains(e.target)) {
            header.classList.remove('search-active');
        }
    });
});

const swimsuitModule = {
    // 추천 수모를 불러와서 화면에 그리는 함수
    initRecommendCaps: function(swimsuitId, colors) {
        console.log(swimsuitId);
        console.log(colors);
        const data = {
            itemId: swimsuitId,
            colors: colors
        };

        // 기존에 만드신 공통 ajax 호출
        oper.ajax("POST", data, "/api/swimsuits/"+swimsuitId+"/recommended-swimcaps", callback.recommendCaps);
    },

    // 데이터를 받아서 HTML을 생성하는 함수
    renderCaps: function(caps) {
        const $container = $('#recommend-cap-list');
        if ($container.length === 0) return; // 요소가 없으면 중단

        $container.empty();
        if (caps && caps.length > 0) {
//            console.log(caps);
            caps.forEach(cap => {
                // 1. 색상 칩 HTML을 먼저 생성합니다.
                let colorChipsHtml = '';
                if (cap.colors && cap.colors.length > 0) {
                    cap.colors.forEach(color => {
                        // 서버에서 보냈던 타임리프 로직을 JS 문법으로 대체
                        colorChipsHtml += `
                            <span class="color-chip"
                                  style="background-color: ${color};"
                                  data-color="${color}"></span>`;
                    });
                }

                $container.append(`
                    <div class="cap-item">
                        <img src="${cap.imageUrl}" alt="${cap.name}">
                        <div class="cap-info">
                            <p>${cap.brand}</p>
                            <p>${cap.name}</p>
                            <p>${Number(cap.price).toLocaleString()}원</p>
                        </div>

                        <div class="color-palette">
                            <span>대표 색상:</span>
                            <div class="chips-wrapper">
                                ${colorChipsHtml}
                            </div>
                        </div>
                    </div>


                `);
            });
        } else {
            $container.append('<p>추천할 수모가 없습니다.</p>');
        }
    }
};

var callback = {
    recommendCaps : function(response) {
        swimsuitModule.renderCaps(response);
    }

}

var oper = {
    isEmpty : function(value) {
        if(value == "" || value == null || value == undefined) {
            return true;
        } else {
            return false;
        }
    },

    ajax : function(type, data, url, callback) {
        // GET이면 쿼리스트링으로 붙이고 data는 제거
        if (type === "GET" && data && Object.keys(data).length > 0) {
            const queryString = $.param(data); // itemId=123&bookingStatus=BOOK
            url += (url.includes("?") ? "&" : "?") + queryString;
            data = null;
        }

        $.ajax({
            'type': type,
            'url':url,
            'dataType':'json',
            'contentType':'application/json; charset=utf-8',
            'data': type === "GET" ? null : JSON.stringify(data)
        })
        .done(function(response){
            callback(response);
        })
        .fail(function(xhr, status, error) {
            // 요청이 실패했을 때 실행되는 코드
            console.error('요청 실패:', xhr);
            let errMsg = xhr.responseJSON?.message || xhr.responseText || error || '알 수 없는 오류';

            // Validation 에러인 경우 필드별 에러도 표시
            if (xhr.responseJSON?.errors) {
                let fieldErrors = xhr.responseJSON.errors;
                let errorDetails = '\n';
                for (let field in fieldErrors) {
                    errorDetails += `- ${fieldErrors[field]}\n`;
                }
                errMsg += errorDetails;
            }

            alert(errMsg);
        })
//        .always(function(){
//            console.log("ajax always 로그");
//        });
    },

    getTodayDt : function() {
        let today = new Date();
        let year = String(today.getFullYear());
        let month = String(today.getMonth()+1).padStart(2,"0");
        let date = String(today.getDate()).padStart(2,"0");
        let hours = String(today.getHours()).padStart(2,"0");
        let minutes = String(today.getMinutes()).padStart(2,"0");
        return year+month+date+hours+minutes;
    },

    getSevenDaysAgo : function() {
        let today = new Date();

        let sevenDaysAgo = new Date();
        sevenDaysAgo.setDate(today.getDate() - 7);

        let year = String(sevenDaysAgo.getFullYear());
        let month = String(sevenDaysAgo.getMonth()+1).padStart(2,"0");
        let date = String(sevenDaysAgo.getDate()).padStart(2,"0");
        let hours = String(sevenDaysAgo.getHours()).padStart(2,"0");
        let minutes = String(sevenDaysAgo.getMinutes()).padStart(2,"0");

        return year+month+date+hours+minutes;
    }
}

main.init();